package com.nice;

import com.opencsv.bean.CsvBindByName;

public class Result {
  private final Record start;
  private final Record end;

  @CsvBindByName()
  private final int utc_hour;

  @CsvBindByName()
  private final long diff;

  @CsvBindByName()
  private final long contact_id;

  public Result(Record startRecord, Record endRecord) {
    this.start = startRecord;
    this.end = endRecord;

    this.utc_hour = endRecord.getHour();
    this.diff = endRecord.getTimeInstant().getMillis() - startRecord.getTimeInstant().getMillis();
    this.contact_id = endRecord.getContact_id();
  }

  public int getHour() {
    return utc_hour;
  }

  public long getDiff() {
    return diff;
  }

  public long getContact_id() {
    return contact_id;
  }

  public String toString() {
    return contact_id + " : " + diff + " : " + utc_hour + "->" + this.end.getLogtime();
  }
}
