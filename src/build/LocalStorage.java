package build;

import java.io.BufferedReader;
import java.io.BufferedWriter;
import java.io.File;
import java.io.FileWriter;
import java.io.IOException;
import java.nio.file.Files;
import java.nio.file.Path;
import java.nio.file.Paths;
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
      System.out.println("File found: " + file.getName() + ".");
      return file;
    } catch (Exception e) {
      e.printStackTrace();
    }
    return null;
  }

  public synchronized void updateStore(String jsonObject, String csID) {
    try {
      LocalDateTime date = LocalDateTime.now();
      // DateTimeFormatter formatter = DateTimeFormatter.ofPattern(
      //   "yyyy MM dd ss AA"
      // );
      // String dateString = date.format(formatter);
      // int valueOffset = 8;
      // String id = jsonObject.substring(
      //   jsonObject.indexOf("\"id\"") + valueOffset,
      //   jsonObject.indexOf("\",\n")
      // );
      // id.replace("\"id\" : \"", "");
      FileWriter fileWriter = new FileWriter("localStorage.txt", true);
      fileWriter.write(csID + ";" + date + '\n');
      fileWriter.write(jsonObject);
      fileWriter.close();
      numEntries++;
      System.out.println(
        "Successfully wrote to the file. " + numEntries + " Entries."
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
    File file = new File("localStorage.txt");

    if (this.exists()) {
      try (
        BufferedReader bufferedReader = Files.newBufferedReader(file.toPath())
      ) {
        String line = bufferedReader.readLine();
        while (line != null) {
          if (line.contains(stationID)) {
            numEntries++;
          }
          line = bufferedReader.readLine();
        }
      }
    }
    return numEntries;
  }

  public synchronized void removeEntries(String csID) throws IOException {
    // System.out.println("Remove : csID : " + csID);
    Path file = Paths.get("localStorage.txt");
    Path tmp = Paths.get("tmp");

    try (
      BufferedReader bufferedReader = Files.newBufferedReader(file);
      BufferedWriter writer = Files.newBufferedWriter(tmp);
    ) {
      String line = bufferedReader.readLine();
      while (line != null) {
        if (line.contains(csID)) {
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
          System.out.println(line);

          jsonObject += "{" + '\n';
          while (!line.equals("}")) {
            jsonObject += line + '\n';
            line = bufferedReader.readLine();
            // System.out.println("l : " + line);
            if (line == null) {
              break;
            }
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

    // System.out.println("Returning: " + jsonObject);
    return jsonObject;
  }
}

class Entry {

  String jsonObject;
  LocalDateTime date;

  Entry(String jsonObject, LocalDateTime date) {
    this.jsonObject = jsonObject;
    this.date = date;
  }
}
