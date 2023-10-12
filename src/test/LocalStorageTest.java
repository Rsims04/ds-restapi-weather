package test;

import build.LocalStorage;
import java.io.File;
import java.io.IOException;

public class LocalStorageTest {

  LocalStorage ls = new LocalStorage();

  String PASS = "\u001B[32m" + "PASS" + "\u001B[0m\n-";
  String FAIL = "\u001B[31m" + "FAIL" + "\u001B[0m\n-";
  int testsPassed = 0;
  int testsFailed = 0;

  public void create_store_file_does_not_exists_test() {
    testsFailed++;
    System.out.println("\n1. create_store_file_does_not_exists_test: ");
    String outcome;
    File file = new File("localStorage.txt");
    file.delete();
    ls.createStore();
    if (ls.exists()) {
      outcome = this.PASS;
      testsPassed++;
      testsFailed--;
    } else {
      outcome = this.FAIL;
    }
    System.out.print(outcome);
  }

  public void create_store_file_does_exist_test() {
    testsFailed++;
    System.out.println("\n2. create_store_file_does_exist_test: ");
    String outcome;
    ls.createStore();
    if (ls.exists()) {
      outcome = this.PASS;
      testsPassed++;
      testsFailed--;
    } else {
      outcome = this.FAIL;
    }
    System.out.print(outcome);
  }

  public void get_store_test() {
    testsFailed++;
    System.out.println("\n3. get_store_test: ");
    String outcome = this.FAIL;
    File file = ls.getStore();
    if (file.exists()) {
      outcome = this.PASS;
      testsPassed++;
      testsFailed--;
      System.out.println(file.getName() + " exists.");
    }
    System.out.print(outcome);
  }

  public void update_store_one_entry_test() throws IOException {
    testsFailed++;
    System.out.println("\n4. update_store_one_entry_test: ");
    String outcome = this.FAIL;
    ls.updateStore("{\n\"id\":\"IDS1\"\n}\n", "csID");
    if (ls.getNumEntries(null) == 1) {
      outcome = this.PASS;
      testsPassed++;
      testsFailed--;
    }
    System.out.print(outcome);
  }

  public void update_store_two_entry_test() throws IOException {
    testsFailed++;
    System.out.println("\n5. update_store_two_entry_test: ");
    String outcome = this.FAIL;
    File delete = new File("localStorage.txt");
    delete.delete();
    ls.updateStore("{\n\"id\":\"IDS1\"\n}\n", "csID");
    ls.updateStore("{\n\"id\":\"IDS1\"\n}\n", "csID");
    if (ls.getNumEntries(null) == 2) {
      outcome = this.PASS;
      testsPassed++;
      testsFailed--;
    }
    System.out.print(outcome);
  }

  public void update_store_failed_entry_test() throws IOException {
    testsFailed++;
    System.out.println("\n6. update_store_failed_entry_test: ");
    String outcome = this.FAIL;
    File delete = new File("localStorage.txt");
    delete.delete();
    ls.updateStore("", "");

    if (ls.getNumEntries(null) == 0) {
      outcome = this.PASS;
      testsPassed++;
      testsFailed--;
    }
    System.out.print(outcome);
  }

  public void get_num_entries_test() throws IOException {
    testsFailed++;
    System.out.println("\n7. get_num_entries_test: ");
    String outcome = this.FAIL;
    File delete = new File("localStorage.txt");
    delete.delete();
    ls.updateStore("{\n\"id\":\"IDS1\"\n}\n", "csID");
    ls.updateStore("{\n\"id\":\"IDS1\"\n}\n", "csID");

    if (ls.getNumEntries(null) == 2) {
      outcome = this.PASS;
      testsPassed++;
      testsFailed--;
    }
    System.out.print(outcome);
  }

  public void remove_entries_test() throws IOException {
    testsFailed++;
    System.out.println("\n8. remove_entries_test: ");
    String outcome = this.FAIL;
    File delete = new File("localStorage.txt");
    delete.delete();
    ls.updateStore("{\n\"id\":\"IDS1\"\n}\n", "csID");
    ls.removeEntries("csID");

    if (ls.getNumEntries(null) == 0) {
      outcome = this.PASS;
      testsPassed++;
      testsFailed--;
    }
    System.out.print(outcome);
  }

  public void get_current_entry_test() throws IOException {
    testsFailed++;
    System.out.println("\n9. get_current_entry_test: ");
    String outcome = this.FAIL;
    File delete = new File("localStorage.txt");
    delete.delete();
    ls.updateStore("{\n\"id\":\"IDS1\"\n\"name\":\"1\"\n}\n", "csID1");
    ls.updateStore("{\n\"id\":\"IDS1\"\n\"name\":\"2\"\n}\n", "csID2");

    String expected = "{\n\"id\":\"IDS1\"\n\"name\":\"2\"\n}\n";
    String actual = ls.getCurrentEntry("IDS1");
    System.out.println("expected:\n" + expected);
    System.out.println("actual:\n" + actual);
    if (actual.equals(expected)) {
      outcome = this.PASS;
      testsPassed++;
      testsFailed--;
    }
    System.out.print(outcome);
  }

  public static void main(String[] args) throws IOException {
    LocalStorageTest test = new LocalStorageTest();

    System.out.print(
      "\n==============================\nRunning: Local Storage Tests\n==============================\n"
    );
    test.create_store_file_does_not_exists_test();
    test.create_store_file_does_exist_test();
    test.get_store_test();
    test.update_store_one_entry_test();
    test.update_store_two_entry_test();
    test.update_store_failed_entry_test();
    test.get_num_entries_test();
    test.remove_entries_test();
    test.get_current_entry_test();

    if (test.testsFailed <= 0) {
      System.out.println("\n\nOK (" + test.testsPassed + " passed)");
    } else {
      System.out.println(
        "\n\nFAIL (" +
        test.testsPassed +
        " passed, " +
        test.testsFailed +
        " failed)\n\n"
      );
    }
    System.out.println("_____________________________\n\n");

    File file = new File("localStorage.txt");
    file.delete();
  }
}
