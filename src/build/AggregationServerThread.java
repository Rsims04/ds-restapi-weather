package build;

import java.io.*;
import java.net.*;

class AggregationServerThread extends Thread {

  private int threadID;
  private Socket clientSocket;
  private BufferedReader in;
  private PrintWriter out;
  private LocalStorage localStorage;
  private File localFile;
  private String stationID;

  /**
   * Constructor
   */
  public AggregationServerThread(
    int threadID,
    Socket clientSocket,
    BufferedReader in,
    PrintWriter out,
    LocalStorage localStorage,
    File localFile
  ) {
    this.threadID = threadID;
    this.clientSocket = clientSocket;
    this.in = in;
    this.out = out;
    this.localStorage = localStorage;
    this.localFile = localFile;
  }

  /**
   *
   */
  @Override
  public void run() {
    while (true) {
      String line;
      try {
        PrintWriter writer = new PrintWriter(out, true);
        line = in.readLine();

        if (line == null) {
          break;
        }

        // System.out.println(line);

        // Process Client GET Request
        if (line.contains("GET")) {
          if (line.contains("?")) {
            stationID =
              line.substring(line.indexOf("=") + 1, line.indexOf(" H"));
          }

          // '/' GET all weather data

          // '/?stationID=STATIONID' GET weather data for specific station

          // Send Weather Data To Client
          writer.println("200 - OK");
          break;
          // Process Content Server PUT Request
        } else if (line.contains("PUT")) {
          // Read header - validate input
          int contentLength = 0;
          while (!line.equals("")) {
            System.err.println(line);
            line = in.readLine();
            // Extract Content Length
            if (line.contains("Content-Length")) {
              contentLength = Integer.parseInt(line.replaceAll("\\D+", ""));
            }
          }
          line = in.readLine();

          // Extract JSON object from input
          if (line.contains("{")) {
            String jsonObject = "";
            int readLength = 0;
            while (readLength <= contentLength || line == null) {
              line += "\n";
              jsonObject += line;
              readLength += line.getBytes().length;

              if (readLength < contentLength) {
                line = in.readLine();
              }
            }
            System.err.println("\n" + jsonObject);

            // Check if valid JSON
            JSONParser j = new JSONParser();
            System.err.println(j.validateJSON(jsonObject));
            if (j.validateJSON(jsonObject)) {
              // If valid create/update local storage file
              if (this.localStorage.exists()) {
                this.localFile = this.localStorage.getStore();
                writer.println("200 - OK");
              } else {
                this.localFile = this.localStorage.createStore();
                writer.println("201 - HTTP_CREATED");
              }
              localStorage.updateStore(jsonObject);
              break;
            } else {
              writer.println("500 - Internal server error");
            }
          } else {
            // Sending no content to the server
            writer.println("204 - No Content");
            break;
          }
          // Your server is designed to stay current
          // and will remove any items in the JSON
          // that have come from content servers which
          // it has not communicated with for 30 seconds

          // then before the content server lost connection,
          // - all other succeed response should use 200

        } else {
          // Returns Status 400
          writer.println("400 - Bad Request");
          continue;
        }
      } catch (IOException e) {
        e.printStackTrace();
      }
    }
    try {
      System.out.println("\n--- \nThread " + threadID + " Closed\n---\n");
      in.close();
      out.close();
    } catch (IOException e) {
      e.printStackTrace();
    }
  }
}
