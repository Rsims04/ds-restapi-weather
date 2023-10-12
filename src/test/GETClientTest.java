package test;

import build.GETClient;

public class GETClientTest {

  GETClient gc = new GETClient();

  String PASS = "\u001B[32m" + "PASS" + "\u001B[0m\n-";
  String FAIL = "\u001B[31m" + "FAIL" + "\u001B[0m\n-";
  int testsPassed = 0;
  int testsFailed = 0;

  public void get_input_success_test() {
    testsFailed++;
    System.out.println("\n1. get_input_success_test: ");
    String outcome = this.FAIL;

    String[] array = { "localhost:4567", "IDS1" };
    gc.getInput(array);

    if (
      gc.getServerName().equals("127.0.0.1") &&
      gc.getPortNumber() == 4567 &&
      gc.getStationID().equals("?stationID=IDS1")
    ) {
      outcome = this.PASS;
      testsPassed++;
      testsFailed--;
    }
    System.out.print(outcome);
  }

  public void get_input_fail_test() {
    testsFailed++;
    System.out.println("\n2. get_input_fail_test: ");
    String outcome = this.FAIL;

    String[] array = { "EEEEE", "EEEEE" };
    gc.getInput(array);

    if (gc.getServerName().equals("127.0.0.1") && gc.getPortNumber() == 4567) {
      outcome = this.PASS;
      testsPassed++;
      testsFailed--;
    }
    System.out.print(outcome);
  }

  public void get_input_server_name_format_test() {
    testsFailed++;
    System.out.println("\n3. get_input_server_name_format_test: ");
    String outcome = this.FAIL;

    Boolean pass = false;
    String[] array = { "localhost:4567" };
    gc.getInput(array);

    if (gc.getServerName().equals("127.0.0.1") && gc.getPortNumber() == 4567) {
      pass = true;
    }
    String[] array2 = { "https://localhost:4567" };
    gc.getInput(array2);
    if (gc.getServerName().equals("127.0.0.1") && gc.getPortNumber() == 4567) {
      pass = true;
    }
    String[] array3 = { "https://localhost.com.au:4567" };
    gc.getInput(array3);

    if (gc.getServerName().equals("127.0.0.1") && gc.getPortNumber() == 4567) {
      pass = true;
    }

    if (pass == true) {
      outcome = this.PASS;
      testsPassed++;
      testsFailed--;
    }
    System.out.print(outcome);
  }

  public static void main(String[] args) {
    GETClientTest test = new GETClientTest();

    System.out.print(
      "\n==============================\nRunning: GETClient Tests\n==============================\n"
    );
    test.get_input_success_test();
    test.get_input_fail_test();
    test.get_input_server_name_format_test();

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
  }
}
