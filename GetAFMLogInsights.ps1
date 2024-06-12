$query = @"
fields @timestamp, log
| PARSE log "*=* *=* *=* - *" as c1, logtime, c2,loglevel,c3,logger, message
| parse message 'contact_id: * ' as contact_id
| parse message 'cluster: "*"' as cluster
| DISPLAY logtime,logger,contact_id,cluster,message
| limit 10000
| filter kubernetes.namespace_name = 'apps-routing'
| filter kubernetes.container_name = 'routing-ng-requests-distributor'
     or kubernetes.container_name = 'routing-ng-agent-selector'
| filter cluster like 'C48'
| filter message like 'bus_no: 4602388'
# | filter message like '508741473514'
| filter (message like 'Proto event' and message like 'CONTACT_ROUTABLE_TYPE_ROUTABLE')
      or message like 'Calling REST api for EAIR service'
| sort @timestamp asc
"@

$startDate = Get-Date -Year 2024 -Month 5 -Day 30 -Hour 0 -Minute 0 -Second 0 -Millisecond -0
$numMinutes = 60 * 24 * 1

Write-Host "Getting $($numMinutes / 60) hours, starting at $startDate"

$start = ([DateTimeOffset]$startDate).ToUnixTimeMilliseconds()
$end = ([DateTimeOffset]($startDate.AddMinutes($numMinutes))).ToUnixTimeMilliseconds()

Write-Host "Starting the query"
$queryID = (aws logs start-query `
        --log-group-name="/aws/eks/shared_eks01/fluentbit-cloudwatch/logs" `
        --start-time $start `
        --end-time $end `
        --query-string $query `
        --profile ic-prod) | ConvertFrom-Json

Write-Host "Checking query status -- QueryID: $queryID"
$done = $False
while (-not $done)
{
    $resJson = aws logs get-query-results --query-id "$($queryID.queryId)" --profile ic-prod
    $res = $resJson | ConvertFrom-Json
    if ($res.status -eq 'Complete')
    {
        $done = $true
        Write-Host "$($res.statistics.recordsMatched) records matched"
        Write-Host "$($res.results.length) records returned"
        $output = $res.results | ForEach-Object { # Each member of the results array is an array itself
            $obj = New-Object -TypeName PSObject; # We're going to create an object with the properties we want
            $_ | Where-Object -Property 'field' -In -Value 'logtime', 'logger', 'contact_id', "cluster", "message" | # For each element in the sub-array, get the objects that represent the fields we care about
            ForEach-Object {
                $obj | Add-Member -MemberType NoteProperty -Name $_.field -Value $_.value.replace('"','') # Add each of those fields to the new object
            }
            $obj # Return each object as we create them
        }

        $path = "Results_$(Get-Date -Format yyyyMMdd_HHmmss).csv"
        $output | Export-Csv $path -Encoding ascii -NoTypeInformation
        Write-Host "Results written to $path"
        Invoke-Item $path
    }
    else
    {
        Write-Host('Checking again')
    }
}
