package build;

/**
 * AggregationServerThread.java
 * These thread are used to process individual requests.
 * Return status codes 200, 201, 204, 400, 500.
 */

import java.io.*;
import java.net.*;
import java.util.ArrayList;
import java.util.Date;

class AggregationServerThread extends Thread {

  private String csID;
  private int threadID;
  private Socket clientSocket;
  private BufferedReader in;
  private PrintWriter out;
  private LocalStorage localStorage;
  private String stationID;
  private LamportClock lc = LamportClock.getInstance();
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

  /**
   * Returns stationID from input.
   * null if none provided.
   */
  public String extractStationID(String line) {
    if (line.contains("ID")) {
      if (line.contains("?")) {
        return line.substring(line.indexOf("=") + 1, line.indexOf(" H"));
      }
      return line.substring(line.indexOf("ID"), line.indexOf(" H"));
    }
    return null;
  }

  /**
   * Returns a formatted http header.
   * With status code provided.
   */
  public String craftHeader(int code) {
    String header = "";
    String type = "";
    String message = "";
    Date date = new Date();

    switch (code) {
      case (200):
        type = "text/plain; charset=UTF-8";
        message = "OK";
        break;
      case (201):
        type = "text/plain; charset=UTF-8";
        message = "Created";
        break;
      case (204):
        type = null;
        message = "No Content";
        break;
      case (400):
        type = "application/json";
        message = "Bad Request";
        break;
      case (500):
        type = null;
        message = "Internal Server Error";
        break;
    }

    header = "HTTP/1.1 " + code + " " + message + "\r\n";
    if (type != null) {
      header += "Date: " + date + "\r\n";
      header += "Content-Type: " + type + "\r\n";
      header += "Content-Language: en" + "\r\n";
    }
    header += "\r\n";
    return header;
  }

  /**
   * Processes a GET request.
   * Returns status 204 for no content.
   * Returns status 200 OK along with:
   * - most current text data from requested stationID.
   * - Most current data from all stationIDs, if no stationID provided.
   */
  public String processGET(String stationID) throws IOException {
    String response = "";
    // GET all weather data
    if (stationID == null) {
      System.out.println("Getting /");
      if (localStorage.getNumEntries(stationID) < 1) {
        response = craftHeader(204);
      } else {
        ArrayList<String> jsons = localStorage.getAllCurrentEntries();
        response = craftHeader(200);
        int i = 1;
        for (String json : jsons) {
          if (json != null) {
            response += j.fromJSON(json);
            if (i < jsons.size()) {
              response += '\n';
            }
          }
          i++;
        }
        response = response.trim();
      }
    } else {
      // GET weather data for specific station
      if (localStorage.getNumEntries(stationID) < 1) {
        response = craftHeader(204);
      } else {
        response = localStorage.getCurrentEntry(stationID);
        response = craftHeader(200) + j.fromJSON(response);
      }
    }
    return response;
  }

  /**
   * Extracts and Returns length of content from PUT header.
   */
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
    return contentLength;
  }

  /**
   *  Extracts and Returns JSON data from PUT request.
   */
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
   * Will run the thread on start().
   * Reads and processes requests.
   * Sending a response code and message to the client.
   */
  @Override
  public synchronized void run() {
    try {
      PrintWriter writer = new PrintWriter(out, true);
      String response = "";
      while (true) {
        String line;
        line = in.readLine();

        if (line == null) {
          break;
        }

        if (line.contains("GET") && !line.contains("favicon")) {
          // Process Client GET Request
          stationID = extractStationID(line);
          response = processGET(stationID);
          break;
        } else if (line.contains("PUT")) {
          // Process Content Server PUT Request
          int contentLength = extractContentLength(in, line);

          // Extract JSON object from input
          String jsonObject = "";
          line = in.readLine();
          if (line.contains("{")) {
            jsonObject = extractJSON(in, line, contentLength);

            System.err.println(jsonObject);

            // If valid JSON create/update local storage file
            if (j.validateJSON(jsonObject)) {
              if (this.localStorage.exists()) {
                this.localStorage.getStore();
                response = craftHeader(200);
              } else {
                this.localStorage.createStore();
                response = craftHeader(201);
              }
              localStorage.updateStore(jsonObject, csID);
              break;
            } else {
              // Internal Server Error
              response = craftHeader(500);
              break;
            }
          } else {
            // Sending no content to the server
            response = craftHeader(204);
            break;
          }
        } else {
          // Bad Request
          response = craftHeader(400);
          break;
        }
      }
      // Send Response/Data To Client
      writer.println(response);
    } catch (IOException e) {
      e.printStackTrace();
    }

    try {
      // Close the inputs and outputs when done.
      System.out.println("\n--- \nThread " + threadID + " Closed\n---\n");
      in.close();
      out.close();
    } catch (IOException e) {
      e.printStackTrace();
    }
  }
}
