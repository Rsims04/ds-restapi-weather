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

  private int csID = (int) (Math.random() * 10000);
  private Socket contentSocket;
  private PrintWriter out;
  private BufferedReader in;
  private JSONParser jp = new JSONParser();
  private LamportClock lc = LamportClock.getInstance();
  private ArrayList<String> putRequests = new ArrayList<String>();

  private String serverName = "localhost";
  private int portNumber = 4567;
  private String filePath;

  public ContentServer() {}

  public String getServerName() {
    return serverName;
  }

  public int getPortNumber() {
    return portNumber;
  }

  public String getFilePath() {
    return filePath;
  }

  public ArrayList<String> getPutRequests() {
    return putRequests;
  }

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
   * Processes and formats command line arguments.
   * Gets:
   * - Server name
   * - Port number
   */
  public void getInput(String[] args) {
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
      filePath = args[1];
    } catch (Exception e) {
      e.printStackTrace();
    }
  }

  /**
   * Adds a PUT header to each request.
   * Splits into array (putRequests)
   */
  public void formatAndSplitRequest() throws IOException {
    System.out.println(filePath);
    File file = new File(filePath);
    // Need to account for multiple data in one file
    ArrayList<String> jsonStringArray = jp.toJSON(file);
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
   * Disconnects and closes connection.
   */
  public void disconnect() throws IOException {
    this.in.close();
    this.out.close();
    this.contentSocket.close();
  }

  public void sendRequests() {
    int index = 1;
    for (String request : putRequests) {
      try {
        String threadID = csID + "-" + index;
        System.out.println("\n---\nNew Thread - id:" + threadID + "\n---\n ");

        Thread t = new ContentServerThread(
          threadID,
          serverName,
          portNumber,
          csID,
          this,
          lc,
          csID,
          request
        );
        System.out.print("\n--- starting: " + csID + "-" + index + " ---\n");
        t.start();
        t.join();

        if (index != putRequests.size()) {
          // Wait 30 seconds before repeat.
          Thread.sleep(30000);
        }
      } catch (InterruptedException e) {
        e.printStackTrace();
      }
      index++;
    }
  }

  public static void main(String[] args)
    throws UnknownHostException, IOException {
    ContentServer content = new ContentServer();

    // Get input from command line
    content.getInput(args);

    // Attach header(s) and split if multiple requests
    content.formatAndSplitRequest();

    // Send each individual request to aggregation server and await response.
    content.sendRequests();
  }
}
