package build;

/**
 * AggregationServerThread.java
 * [Description Here]
 */

import java.io.*;
import java.net.*;
import java.util.ArrayList;

class AggregationServerThread extends Thread {

  private String csID;
  private int threadID;
  private Socket clientSocket;
  private BufferedReader in;
  private PrintWriter out;
  private LocalStorage localStorage;
  private String stationID;
  JSONParser j = new JSONParser();

  /**
   * Constructor
   */
  public AggregationServerThread(
    String csID,
    int threadID,
    Socket clientSocket,
    BufferedReader in,
    PrintWriter out,
    LocalStorage localStorage
  ) {
    this.csID = csID;
    this.threadID = threadID;
    this.clientSocket = clientSocket;
    this.in = in;
    this.out = out;
    this.localStorage = localStorage;
  }

  public String extractStationID(String line) {
    if (line.contains("ID")) {
      if (line.contains("?")) {
        return line.substring(line.indexOf("=") + 1, line.indexOf(" H"));
      }
      return line.substring(line.indexOf("ID"), line.indexOf(" H"));
    }
    return null;
  }

  public String processGET(String stationID) throws IOException {
    String response = "";
    // '/' GET all weather data
    if (stationID == null) {
      // writer.println("200 - OK");
      System.out.println("Getting /");
      if (localStorage.getNumEntries(stationID) < 1) {
        response = "204 - No Content";
      } else {
        ArrayList<String> jsons = localStorage.getAllCurrentEntries();
        response = "200 - OK";
        for (String json : jsons) {
          response += "\n\n" + j.fromJSON(json);
        }
      }
    } else {
      // GET weather data for specific station
      // Find most recent requested station data
      // - Most recent is latest sent PUT (NOT latest received)

      if (localStorage.getNumEntries(stationID) < 1) {
        response = "204 - No Content";
      } else {
        response = localStorage.getCurrentEntry(stationID);
        response = "200 - OK\n\n" + j.fromJSON(response);
      }
    }
    return response;
  }

  public int extractContentLength(BufferedReader in, String line)
    throws IOException {
    int contentLength = 0;
    while (!line.equals("")) {
      System.err.println(line);
      line = in.readLine();
      // Extract Content Length
      if (line.contains("Content-Length")) {
        contentLength = Integer.parseInt(line.replaceAll("\\D+", ""));
      }
    }
    // line = in.readLine();
    return contentLength;
  }

  public String extractJSON(BufferedReader in, String line, int contentLength)
    throws IOException {
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
    return jsonObject;
  }

  /**
   *
   */
  @Override
  public synchronized void run() {
    while (true) {
      String line;
      try {
        PrintWriter writer = new PrintWriter(out, true);
        line = in.readLine();

        if (line == null) {
          break;
        }

        if (line.contains("GET") && !line.contains("favicon")) {
          // Process Client GET Request
          stationID = extractStationID(line);
          String response = processGET(stationID);

          // Send Weather Data To Client
          writer.println(response);
          break;
        } else if (line.contains("PUT")) {
          // Process Content Server PUT Request
          int contentLength = extractContentLength(in, line);

          // Extract JSON object from input
          String jsonObject = "";
          line = in.readLine();
          if (line.contains("{")) {
            jsonObject = extractJSON(in, line, contentLength);

            System.err.println("\n" + jsonObject);

            // If valid JSON create/update local storage file
            if (j.validateJSON(jsonObject)) {
              if (this.localStorage.exists()) {
                this.localStorage.getStore();
                writer.println("200 - OK");
              } else {
                this.localStorage.createStore();
                writer.println("201 - HTTP_CREATED");
              }
              localStorage.updateStore(jsonObject, csID);
              break;
            } else {
              writer.println("500 - Internal server error");
            }
          } else {
            // Sending no content to the server
            writer.println("204 - No Content");
            break;
          }
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
