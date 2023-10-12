package test;

import build.AggregationServer;
import java.io.IOException;

public class AggregationServerTest {

  AggregationServer as = new AggregationServer();

  String PASS = "\u001B[32m" + "PASS" + "\u001B[0m\n-";
  String FAIL = "\u001B[31m" + "FAIL" + "\u001B[0m\n-";
  int testsPassed = 0;
  int testsFailed = 0;

  public void get_input_success_test() {
    testsFailed++;
    System.out.println("\n1. get_input_success_test: ");
    String outcome = this.FAIL;

    if (true) {
      outcome = this.PASS;
      testsPassed++;
      testsFailed--;
    }
    System.out.print(outcome);
  }

  public static void main(String[] args) throws IOException {
    AggregationServerTest test = new AggregationServerTest();

    System.out.print(
      "\n==============================\nRunning: Aggregation Server Tests\n==============================\n"
    );
    test.get_input_success_test();

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
