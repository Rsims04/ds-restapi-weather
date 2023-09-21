/**
 * ContentServer.java
 * Content server will:
 * - Take input from a file (weather station) through the command line.
 * - Process inputs to JSON format.
 * - And send each individual result to the Aggregation Server every 30 seconds.
 *
 * Has a Lamport Clock.
 */
package build;

import java.io.BufferedReader;
import java.io.File;
import java.io.IOException;
import java.io.InputStreamReader;
import java.io.PrintWriter;
import java.net.InetAddress;
import java.net.Socket;
import java.net.UnknownHostException;
import java.util.ArrayList;

public class ContentServer {

  private Integer csID = (int) (Math.random() * 10000);
  private Socket contentSocket;
  private PrintWriter out;
  private BufferedReader in;
  private JSONParser jp = new JSONParser();
  private LamportClock lc = new LamportClock();
  private ArrayList<String> putRequests = new ArrayList<String>();

  public ContentServer() {}

  /**
   * Connect to server at specified ip, port.
   */
  public void connect(String ip, int port)
    throws UnknownHostException, IOException {
    this.contentSocket = new Socket(ip, port);
    this.out = new PrintWriter(this.contentSocket.getOutputStream(), true);
    this.in =
      new BufferedReader(new InputStreamReader(contentSocket.getInputStream()));
  }

  /**
   * Adds a PUT header to each request.
   * Splits into array (putRequests)
   */
  public void formatAndSplitRequest(
    ArrayList<String> jsonStringArray,
    String filePath,
    String serverName,
    int portNumber
  ) throws UnknownHostException, IOException {
    for (String jsonObject : jsonStringArray) {
      // Craft Header
      String header = "PUT /" + filePath + " HTTP/1.1\n";
      String userAgent = "ATOMClient/1/" + csID + "\n";
      String contentType = "application/json\n";
      String contentLength = jsonObject.getBytes().length + "\n";

      header += "User-Agent: " + userAgent;
      header += "Content-Type: " + contentType;
      header += "Content-Length: " + contentLength;
      header += "\n";

      String request = header + jsonObject;

      putRequests.add(request);
    }
  }

  /**
   * Send message to server.
   * Update Lamport Clock.
   * Returns response.
   */
  public String sendMsg(String msg) throws IOException {
    lc.sendEvent();
    int clock = lc.getTime();
    System.err.println("CLOCK: " + clock + " ; lc.time(): " + lc.getTime());
    while (lc.getTime() != clock) {
      System.err.println(clock + " CLOCK");
    }
    out.println(csID + "\r" + msg);
    System.err.println(csID);

    String res = in.readLine();
    lc.receiveEvent(lc.getTime());
    return res;
  }

  /**
   * Disconnects and closes connection.
   */
  public void disconnect() throws IOException {
    this.in.close();
    this.out.close();
    this.contentSocket.close();
  }

  public static void main(String[] args)
    throws UnknownHostException, IOException {
    ContentServer content = new ContentServer();
    String serverName = "localhost";
    int portNumber = 4567;

    try {
      // Gets and splits command line input for server name and port number.
      if (args.length > 0) {
        String input = args[0];
        if (input.contains("https://")) {
          input = input.split("//*")[1];
        }

        String splitInput[] = input.split(":\\.*");
        if (splitInput.length <= 2) {
          portNumber = Integer.parseInt(splitInput[1]);
          serverName = splitInput[0];
        } else {
          portNumber = Integer.parseInt(splitInput[2]);
          serverName = splitInput[0] + ":" + splitInput[1];
        }

        try {
          InetAddress ip = InetAddress.getByName(serverName);
          serverName = ip.getHostAddress();
        } catch (UnknownHostException e) {
          e.printStackTrace();
        }
      }

      // Parse file to json format.
      String filePath = args[1];
      File file = new File(filePath);

      // Need to account for multiple data in one file
      ArrayList<String> jsonStringArray = content.jp.toJSON(file);

      // Attach header(s) and split if multiple requests
      content.formatAndSplitRequest(
        jsonStringArray,
        filePath,
        serverName,
        portNumber
      );

      // Send each individual request to aggregation server and await response.
      int index = 1;
      for (String request : content.putRequests) {
        content.connect(serverName, portNumber);
        String res = content.sendMsg(request);
        System.out.println(res);

        if (
          res.contains("200 OK") ||
          res.contains("201 HTTP_CREATED") ||
          res.contains("204 No Content") ||
          res.contains("400 Bad Request") ||
          res.contains("500 Internal Server Error")
        ) {
          content.disconnect();
          if (index != content.putRequests.size()) {
            // Wait 30 seconds before repeat.
            Thread.sleep(30000);
          }
        }
        index++;
      }
    } catch (Exception e) {
      e.printStackTrace();
    }
  }
}
