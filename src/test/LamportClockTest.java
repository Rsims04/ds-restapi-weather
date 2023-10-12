package test;

import build.LamportClock;
import java.io.File;

public class LamportClockTest {

  LamportClock lc = LamportClock.getInstance();

  String PASS = "\u001B[32m" + "PASS" + "\u001B[0m\n-";
  String FAIL = "\u001B[31m" + "FAIL" + "\u001B[0m\n-";
  int testsPassed = 0;
  int testsFailed = 0;

  public void restore_clock_file_not_exist_test() {
    testsFailed++;
    System.out.println("\n1. restore_clock_file_not_exist_test: ");
    String outcome = this.FAIL;
    File file = new File("clock");

    if (file.exists()) {
      outcome = this.PASS;
      testsPassed++;
      testsFailed--;
    }
    System.out.print(outcome);
  }

  public void save_clock_test() {
    testsFailed++;
    System.out.println("\n2. save_clock_test: ");
    String outcome = this.FAIL;
    File file = new File("clock");
    file.delete();
    lc.getTime();
    lc.setTime(1);
    int result = lc.getTime();
    if (file.exists()) {
      if (result == 1) {
        outcome = this.PASS;
        testsPassed++;
        testsFailed--;
      }
    }
    System.out.print(outcome);
  }

  public void get_time_test() {
    testsFailed++;
    System.out.println("\n3. get_time_test: ");
    String outcome = this.FAIL;
    File file = new File("clock");
    file.delete();
    lc.getTime();
    lc.setTime(1);
    int result = lc.getTime();
    if (file.exists()) {
      if (result == 1) {
        outcome = this.PASS;
        testsPassed++;
        testsFailed--;
      }
    }
    System.out.print(outcome);
  }

  public void get_time_zero_test() {
    testsFailed++;
    System.out.println("\n4. get_time_zero_test: ");
    String outcome = this.FAIL;
    File file = new File("clock");
    file.delete();
    lc.getTime();
    int result = lc.getTime();
    if (file.exists()) {
      if (result == 1) {
        outcome = this.PASS;
        testsPassed++;
        testsFailed--;
      }
    }
    System.out.print(outcome);
  }

  public void set_time_test() {
    testsFailed++;
    System.out.println("\n5. set_time_test: ");
    String outcome = this.FAIL;
    File file = new File("clock");
    file.delete();
    lc.getTime();
    lc.setTime(100);
    int result = lc.getTime();
    if (file.exists()) {
      if (result == 100) {
        outcome = this.PASS;
        testsPassed++;
        testsFailed--;
      }
    }
    System.out.print(outcome);
  }

  public void send_event_test() {
    testsFailed++;
    System.out.println("\n6. send_event_test: ");
    String outcome = this.FAIL;
    File file = new File("clock");
    file.delete();
    lc.getTime();
    lc.setTime(1);
    lc.sendEvent();
    int result = lc.getTime();
    if (file.exists()) {
      if (result == 2) {
        outcome = this.PASS;
        testsPassed++;
        testsFailed--;
      }
    }
    System.out.print(outcome);
  }

  public void receive_event_min_test() {
    testsFailed++;
    System.out.println("\n7. receive_event_test: ");
    String outcome = this.FAIL;
    File file = new File("clock");
    file.delete();
    lc.getTime();
    lc.setTime(2);
    lc.receiveEvent(1);
    int result = lc.getTime();
    if (file.exists()) {
      if (result == 3) {
        outcome = this.PASS;
        testsPassed++;
        testsFailed--;
      }
    }
    System.out.print(outcome);
  }

  public void receive_event_max_test() {
    testsFailed++;
    System.out.println("\n8. receive_event_test: ");
    String outcome = this.FAIL;
    File file = new File("clock");
    file.delete();
    lc.getTime();
    lc.setTime(1);
    lc.receiveEvent(2);
    int result = lc.getTime();
    if (file.exists()) {
      if (result == 3) {
        outcome = this.PASS;
        testsPassed++;
        testsFailed--;
      }
    }
    System.out.print(outcome);
  }

  public static void main(String[] args) {
    LamportClockTest test = new LamportClockTest();

    System.out.print(
      "\n==============================\nRunning: Lamport Clock Tests\n==============================\n"
    );
    test.restore_clock_file_not_exist_test();
    test.save_clock_test();
    test.get_time_test();
    test.get_time_zero_test();
    test.set_time_test();
    test.send_event_test();
    test.receive_event_min_test();
    test.receive_event_max_test();

    if (test.testsFailed <= 0) {
      System.out.println("\n\nOK (" + test.testsPassed + " passed)\n");
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

    File file = new File("clock");
    file.delete();
  }
}
