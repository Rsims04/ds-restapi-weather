package build;

import java.io.BufferedReader;
import java.io.File;
import java.io.FileWriter;
import java.io.IOException;
import java.nio.file.Files;
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

  public void updateStore(String jsonObject) {
    try {
      Date date = new Date();
      String id = jsonObject.substring(
        jsonObject.indexOf("\"id\"") + 8,
        jsonObject.indexOf("\",\n")
      );
      // id.replace("\"id\" : \"", "");
      FileWriter fileWriter = new FileWriter("localStorage.txt", true);
      fileWriter.write(id + " " + date + '\n');
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
}
