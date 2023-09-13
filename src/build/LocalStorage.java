package build;

import java.io.File;
import java.io.FileWriter;
import java.io.IOException;

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
      System.out.println("File received: " + file.getName());
      return file;
    } catch (Exception e) {
      e.printStackTrace();
    }
    return null;
  }

  public void updateStore(String jsonObject) {
    try {
      FileWriter fileWriter = new FileWriter("localStorage.txt", true);
      fileWriter.write(jsonObject);
      fileWriter.close();
      System.out.println("Successfully wrote to the file.");
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
}
