package build;

/**
 * LocalStorage.java
 * [Description Here]
 */

import java.io.BufferedReader;
import java.io.BufferedWriter;
import java.io.File;
import java.io.IOException;
import java.nio.file.Files;
import java.nio.file.Path;
import java.nio.file.Paths;
import java.nio.file.StandardOpenOption;
import java.time.Duration;
import java.time.LocalDateTime;
import java.util.ArrayList;

public class LocalStorage {

  int numEntries;

  LocalStorage() {
    this.numEntries = 0;
  }

  public File createStore() {
    try {
      File file = new File("localStorage.txt");
      if (file.createNewFile()) {
        System.out.println("File created: " + file.getName());
        return file;
      } else {
        System.out.println("File already exists.");
        return getStore();
      }
    } catch (IOException e) {
      e.printStackTrace();
    }
    return null;
  }

  public File getStore() {
    try {
      File file = new File("localStorage.txt");
      return file;
    } catch (Exception e) {
      e.printStackTrace();
    }
    return null;
  }

  public synchronized void updateStore(String jsonObject, String csID) {
    try {
      Path file = Paths.get("localStorage.txt");
      LocalDateTime date = LocalDateTime.now();

      BufferedWriter fileWriter = Files.newBufferedWriter(
        file,
        StandardOpenOption.APPEND,
        StandardOpenOption.CREATE
      );
      fileWriter.append(csID + ";" + date + '\n');
      fileWriter.append(jsonObject);
      fileWriter.close();
      numEntries++;
      System.out.println(
        "Successfully wrote to the " + file + ". " + numEntries + " Entries."
      );
    } catch (IOException e) {
      System.out.println("An error occurred.");
      e.printStackTrace();
    }
  }

  public boolean exists() {
    File file = new File("localStorage.txt");
    if (file.exists()) {
      return true;
    }
    return false;
  }

  public int getNumEntries(String stationID) throws IOException {
    numEntries = 0;
    Path file = Paths.get("localStorage.txt");

    if (this.exists()) {
      try (BufferedReader bufferedReader = Files.newBufferedReader(file)) {
        String line = bufferedReader.readLine();
        if (stationID == null) {
          System.out.println("SID is null");
          System.out.println(line);
          while (line != null) {
            if (line.contains("id")) {
              numEntries++;
              System.out.println("processing...");
            }
            line = bufferedReader.readLine();
          }
        } else {
          while (line != null) {
            if (line.contains(stationID)) {
              numEntries++;
            }
            line = bufferedReader.readLine();
          }
        }
      }
    }
    return numEntries;
  }

  public synchronized void removeEntries(String csID) throws IOException {
    Path file = Paths.get("localStorage.txt");
    Path tmp = Paths.get("tmp");

    try (
      BufferedReader bufferedReader = Files.newBufferedReader(file);
      BufferedWriter writer = Files.newBufferedWriter(tmp);
    ) {
      LocalDateTime date = LocalDateTime.now();
      String line = bufferedReader.readLine();
      while (line != null) {
        if (line.contains(";")) {
          date = LocalDateTime.parse(line.split(";")[1]);
        }
        if (
          line.contains(csID) ||
          Duration.between(date, LocalDateTime.now()).toSeconds() > 30
        ) {
          while (!line.equals("}")) {
            line = bufferedReader.readLine();
            continue;
          }
          if (line.equals("}")) {
            line = bufferedReader.readLine();
            continue;
          }
        } else {
          writer.write(line);
          writer.newLine();
        }
        line = bufferedReader.readLine();
      }
      writer.close();

      Files.delete(file);
      Files.move(tmp, file);
    } catch (IOException e) {
      e.printStackTrace();
    }
  }

  public String getCurrentEntry(String stationID) throws IOException {
    System.out.println("Get Entry For: " + stationID);
    ArrayList<Entry> entries = new ArrayList<Entry>();
    String jsonObject = "";
    LocalDateTime date = LocalDateTime.of(1, 1, 1, 0, 0);

    Path file = Paths.get("localStorage.txt");

    try (BufferedReader bufferedReader = Files.newBufferedReader(file);) {
      String line = bufferedReader.readLine();
      String dateString = "";
      while (line != null) {
        if (line.contains(";")) {
          dateString = line.split(";")[1];
          date = LocalDateTime.parse(dateString);
        }
        if (line.contains(stationID)) {
          jsonObject += "{" + '\n';
          while (!line.equals("}")) {
            jsonObject += line + '\n';
            line = bufferedReader.readLine();
          }
          jsonObject += "}" + '\n';

          Entry entry = new Entry(jsonObject, date);
          entries.add(entry);
          System.out.println(jsonObject);
          jsonObject = "";
        }
        line = bufferedReader.readLine();
      }
      date = LocalDateTime.of(1, 1, 1, 1, 1);
      for (Entry entry : entries) {
        if (entry.date.isAfter(date)) {
          jsonObject = entry.jsonObject;
        }
      }
    }
    return jsonObject;
  }

  public ArrayList<String> getAllCurrentEntries() throws IOException {
    System.out.println("Get All Entries: ");
    ArrayList<String> jsonObjects = new ArrayList<String>();
    ArrayList<String> stationIDs = new ArrayList<String>();
    String jsonObject = "";

    Path file = Paths.get("localStorage.txt");

    try (BufferedReader bufferedReader = Files.newBufferedReader(file);) {
      String line = bufferedReader.readLine();
      while (line != null) {
        if (line.contains("\"id\"")) {
          String stationID = line.substring(
            line.indexOf("\"ID") + 1,
            line.indexOf("\",")
          );
          if (!stationIDs.contains(stationID)) {
            stationIDs.add(stationID);
          }
        }
        line = bufferedReader.readLine();
      }
    }

    System.out.println(stationIDs.size());
    for (String stationID : stationIDs) {
      System.out.println(stationID);
      jsonObjects.add(getCurrentEntry(stationID));
    }
    System.out.println("All entry result:\n" + jsonObject);

    return jsonObjects;
  }
}
