package test;

import build.ContentServer;
import build.JSONParser;
import java.io.IOException;

public class ContentServerTest {

  ContentServer cs = new ContentServer();
  JSONParser jp = new JSONParser();

  String PASS = "\u001B[32m" + "PASS" + "\u001B[0m\n-";
  String FAIL = "\u001B[31m" + "FAIL" + "\u001B[0m\n-";
  int testsPassed = 0;
  int testsFailed = 0;

  public void get_input_success_test() {
    testsFailed++;
    System.out.println("\n1. get_input_success_test: ");
    String outcome = this.FAIL;

    String[] array = { "localhost:4567", "input.txt" };
    cs.getInput(array);

    if (
      cs.getServerName().equals("127.0.0.1") &&
      cs.getPortNumber() == 4567 &&
      cs.getFilePath().equals("input.txt")
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
    cs.getInput(array);

    if (cs.getServerName().equals("127.0.0.1") && cs.getPortNumber() == 4567) {
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
    String[] array = { "localhost:4567", "input.txt" };
    cs.getInput(array);

    if (cs.getServerName().equals("127.0.0.1") && cs.getPortNumber() == 4567) {
      pass = true;
    }
    String[] array2 = { "https://localhost:4567", "input.txt" };
    cs.getInput(array2);
    if (cs.getServerName().equals("127.0.0.1") && cs.getPortNumber() == 4567) {
      pass = true;
    }
    String[] array3 = { "https://localhost.com.au:4567", "input.txt" };
    cs.getInput(array3);

    if (cs.getServerName().equals("127.0.0.1") && cs.getPortNumber() == 4567) {
      pass = true;
    }

    if (pass == true) {
      outcome = this.PASS;
      testsPassed++;
      testsFailed--;
    }
    System.out.print(outcome);
  }

  public void format_and_split_test() throws IOException {
    testsFailed++;
    System.out.println("\n4. format_and_split_test: ");
    String outcome = this.FAIL;

    Boolean pass = false;
    String[] array = { "localhost:4567", "input.txt" };
    cs.getInput(array);

    cs.formatAndSplitRequest();

    pass = false;

    if (cs.getPutRequests().size() == 2) {
      pass = true;
    }

    if (pass == true) {
      outcome = this.PASS;
      testsPassed++;
      testsFailed--;
    }
    System.out.print(outcome);
  }

  public static void main(String[] args) throws IOException {
    ContentServerTest test = new ContentServerTest();

    System.out.print(
      "\n==============================\nRunning: Content Server Tests\n==============================\n"
    );
    test.get_input_success_test();
    test.get_input_fail_test();
    test.get_input_server_name_format_test();
    test.format_and_split_test();

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
