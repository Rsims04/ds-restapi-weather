package build;

/**
 * LocalStorage.java
 * LocalStorage is used to store data in a persistant file.
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

  /**
   * Creates the file "localStorage",
   * If the file exists already:
   *  - Returns the current file.
   */
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

  /**
   * Returns the existing file "localStorage.txt"
   */
  public File getStore() {
    try {
      File file = new File("localStorage.txt");
      return file;
    } catch (Exception e) {
      e.printStackTrace();
    }
    return null;
  }

  /**
   * Takes in a JSON object and a content server ID (csID).
   * Writes the JSON object to local storage.
   * While appending the csID and current time.
   */
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

  /**
   * Check whether "localStorage.txt" exists.
   */
  public boolean exists() {
    File file = new File("localStorage.txt");
    if (file.exists()) {
      return true;
    }
    return false;
  }

  /**
   * Returns the number of entries:
   * - Of provided stationID
   * Or if stationID is null
   * - Of all entries.
   */
  public int getNumEntries(String stationID) throws IOException {
    numEntries = 0;
    Path file = Paths.get("localStorage.txt");

    if (this.exists()) {
      try (BufferedReader bufferedReader = Files.newBufferedReader(file)) {
        String line = bufferedReader.readLine();
        if (stationID == null) {
          while (line != null) {
            if (line.contains("id")) {
              numEntries++;
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

  /**
   * Deletes all entries
   * - That have a specified csID.
   * - Or are greater than 30 seconds old.
   */
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

  /**
   * Returns the most current entry:
   * - with specified stationID.
   * - in JSON format.
   */
  public String getCurrentEntry(String stationID) throws IOException {
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

  /**
   * Returns Multiple entrys:
   * - The most current entrys, from all unique stationIDs
   */
  public ArrayList<String> getAllCurrentEntries() throws IOException {
    ArrayList<String> jsonObjects = new ArrayList<String>();
    ArrayList<String> stationIDs = new ArrayList<String>();

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

    for (String stationID : stationIDs) {
      jsonObjects.add(getCurrentEntry(stationID));
    }

    return jsonObjects;
  }
}
