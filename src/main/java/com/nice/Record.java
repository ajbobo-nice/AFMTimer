package com.nice;

import com.opencsv.bean.CsvBindByName;
import org.joda.time.Instant;
import org.joda.time.format.DateTimeFormat;
import org.joda.time.format.DateTimeFormatter;

public class Record {
  private final DateTimeFormatter fmt = DateTimeFormat.forPattern("yyyy-MM-dd'T'HH:mm:ss,SSS");

  @CsvBindByName
  private String logtime;

  private Instant timeInstant;

  @CsvBindByName
  private String logger;

  @CsvBindByName
  private long contact_id;

  @CsvBindByName
  private String cluster;

  @CsvBindByName
  private String message;

  public String getLogtime() {
    return logtime;
  }

  public void setLogtime(String logtime) {
    this.logtime = logtime;

    this.timeInstant = fmt.parseDateTime(logtime).toInstant();
  }

  public int getHour() {
    return fmt.parseDateTime(this.logtime).getHourOfDay();
  }

  public String getLogger() {
    return logger;
  }

  public void setLogger(String logger) {
    this.logger = logger;
  }

  public long getContact_id() {
    return contact_id;
  }

  public void setContact_id(long contact_id) {
    this.contact_id = contact_id;
  }

  public String getCluster() {
    return cluster;
  }

  public void setCluster(String cluster) {
    this.cluster = cluster;
  }

  public String getMessage() {
    return message;
  }

  public void setMessage(String message) {
    this.message = message;
  }

  public Instant getTimeInstant() {
    return timeInstant;
  }
}
