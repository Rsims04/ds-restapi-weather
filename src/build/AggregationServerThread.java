package build;

/**
 * AggregationServerThread.java
 * [Description Here]
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
        for (String json : jsons) {
          response += j.fromJSON(json);
        }
        response = response.strip();
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

            System.err.println("\n" + jsonObject);

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
              response = craftHeader(500);
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
      System.out.println("\n--- \nThread " + threadID + " Closed\n---\n");
      in.close();
      out.close();
    } catch (IOException e) {
      e.printStackTrace();
    }
  }
}
