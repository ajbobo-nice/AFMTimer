package com.nice;

import com.opencsv.bean.CsvToBeanBuilder;
import com.opencsv.bean.StatefulBeanToCsv;
import com.opencsv.bean.StatefulBeanToCsvBuilder;
import com.opencsv.exceptions.CsvDataTypeMismatchException;
import com.opencsv.exceptions.CsvRequiredFieldEmptyException;

import java.io.FileNotFoundException;
import java.io.FileReader;
import java.io.FileWriter;
import java.io.IOException;
import java.io.Writer;
import java.util.LinkedHashMap;
import java.util.LinkedList;
import java.util.List;
import java.util.Map;
import java.util.Queue;

public class Main {
  public static void main(String[] args) {
    String inputFile = "Results_20240612_102119.csv";
    String outputFile = "Contacts_20240612_102119.csv";

    try {
      List<Record> afmRecords = new CsvToBeanBuilder<Record>(new FileReader(inputFile))
          .withType(Record.class)
          .build()
          .parse();

      System.out.println("Number of records read: " + afmRecords.size());

      Map<Long, Queue<Record>> queueList = new LinkedHashMap<>();
      List<Result> resultList = new LinkedList<>();

      for (Record record : afmRecords) {
        if (record.getMessage().startsWith("Proto event")) {
          Queue<Record> contactQueue = queueList.computeIfAbsent(record.getContact_id(), k -> new LinkedList<>());
          contactQueue.add(record);
        }
        else if (record.getMessage().startsWith("Calling REST")) {
          Queue<Record> contactQueue = queueList.getOrDefault(record.getContact_id(), null);
          if (contactQueue != null) {
            Record contactRecord = contactQueue.poll();
            if (contactRecord != null) {
              Result res = new Result(contactRecord, record);
              resultList.add(res);
            }
            else {
              System.out.println("Contact " + record.getContact_id() + " not found");
            }
          }
          else {
            System.out.println("Unable to find queue for contact: " + record.getContact_id());
          }
        }
      }

      Writer writer = new FileWriter(outputFile);
      StatefulBeanToCsv<Result> beanToCsv = new StatefulBeanToCsvBuilder<Result>(writer).build();
      beanToCsv.write(resultList);
      writer.close();
    }
    catch (FileNotFoundException e) {
      System.out.println("File not found: " + inputFile);
    }
    catch (IOException e) {
      System.out.println("I/O error: " + e.getMessage());
    }
    catch (CsvRequiredFieldEmptyException | CsvDataTypeMismatchException e) {
      throw new RuntimeException(e);
    }
  }

}