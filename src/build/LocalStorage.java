package build;

import java.io.BufferedReader;
import java.io.BufferedWriter;
import java.io.File;
import java.io.FileWriter;
import java.io.IOException;
import java.io.PrintWriter;
import java.nio.file.Files;
import java.nio.file.Path;
import java.nio.file.Paths;
import java.util.Date;

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
      numEntries = getNumEntries();
      return file;
    } catch (Exception e) {
      e.printStackTrace();
    }
    return null;
  }

  public synchronized void updateStore(String jsonObject, String csID) {
    try {
      Date date = new Date();
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

  public int getNumEntries() throws IOException {
    numEntries = 0;
    File file = new File("localStorage.txt");

    if (this.exists()) {
      try (
        BufferedReader bufferedReader = Files.newBufferedReader(file.toPath())
      ) {
        String line = bufferedReader.readLine();
        while (line != null) {
          if (line.equals("{")) {
            numEntries++;
          }
          line = bufferedReader.readLine();
        }
      }
    }
    return numEntries;
  }

  public void removeEntries(String csID) throws IOException {
    System.out.println("Remove : csID : " + csID);
    Path file = Paths.get("localStorage.txt");
    Path tmp = Paths.get("tmp.txt");

    try (
      BufferedReader bufferedReader = Files.newBufferedReader(file);
      BufferedWriter writer = Files.newBufferedWriter(tmp);
    ) {
      String line = bufferedReader.readLine();
      while (line != null) {
        System.out.print("looping...");
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
          System.out.println("WRITING: " + line);
          writer.write(line);
          writer.newLine();
        }
        line = bufferedReader.readLine();
      }
      writer.close();

      System.out.println("deleting File");
      Files.delete(file);
      System.out.println("Moving File");
      Files.move(tmp, file);
      // System.out.println("DELETING NOW \n\n\n");
      // if (!file.delete()) {
      //   System.out.println("Could not delete file...");
      // }
      // if (!tmpFile.renameTo(file)) {
      //   System.out.println("Could not rename tmp file...");
      // }
      // if (!tmpFile.delete()) {
      //   System.out.println("Could not delete tmp file...");
      // }
    } catch (IOException e) {
      e.printStackTrace();
    }
  }
}
