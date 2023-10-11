/**
 * GETClient.java
 * Takes in from command line the following:
 * - Address
 * - Port Number
 * - [optional] stationID
 *
 * Connects to and sends a GET request to the aggregation server
 * on specified port, for the current weather data (of stationID if specified).
 *
 * Prints out response.
 */
package build;

import java.io.BufferedReader;
import java.io.IOException;
import java.io.InputStreamReader;
import java.io.PrintWriter;
import java.net.ConnectException;
import java.net.InetAddress;
import java.net.Socket;
import java.net.UnknownHostException;

public class GETClient {

  private Socket clientSocket;
  private PrintWriter out;
  private BufferedReader in;

  public GETClient() {}

  /**
   * Try to connect to server at ip, port.
   */
  public void connect(String ip, int port)
    throws UnknownHostException, IOException {
    this.clientSocket = new Socket(ip, port);
    this.out = new PrintWriter(this.clientSocket.getOutputStream(), true);
    this.in =
      new BufferedReader(new InputStreamReader(clientSocket.getInputStream()));
  }

  /**
   * Send a message to connection.
   * Returns the response.
   */
  public String sendMsg(String msg) throws IOException {
    out.println(msg);

    String res = "";
    String line = in.readLine();
    while (true) {
      if (line == null) {
        break;
      }
      res += line + '\n';
      line = in.readLine();
    }
    return res;
  }

  /**
   *  Client attempts to connect to the known Server.
   *  The client will try 3 times before giving up.
   *  Each attempt in 10 second intervals.
   */
  String run(
    GETClient client,
    String serverName,
    int portNumber,
    String stationID
  ) throws UnknownHostException, IOException {
    String res = "";
    int attempts = 1;
    while (attempts <= 3) {
      try {
        client.connect(serverName, portNumber);
        res = client.sendMsg("GET /" + stationID + " HTTP/1.1");
        break;
      } catch (ConnectException ce) {
        if (attempts == 3) {
          System.err.println(
            "Attempt " + attempts + ": Could not connect. Try again later.\n---"
          );
          break;
        }
        System.err.println(
          "Attempt " +
          attempts +
          ": Could not connect, trying again in 10 seconds.\n---"
        );

        try {
          Thread.sleep(10000);
        } catch (InterruptedException ie) {
          ie.printStackTrace();
        }
      }
      attempts++;
    }
    return res;
  }

  /**
   * Disconnects and closes connection.
   */
  public void disconnect() throws IOException {
    this.in.close();
    this.out.close();
    this.clientSocket.close();
  }

  public static void main(String[] args)
    throws UnknownHostException, IOException {
    GETClient client = new GETClient();
    String serverName = "localhost";
    int portNumber = 4567;
    String stationID = "";

    try {
      // Split input to get: servername, portnumber and stationID
      if (args.length > 0) {
        String input = args[0];
        if (input.contains("https://")) {
          input = input.split("//*")[1];
        }

        if (input.contains(":")) {
          String splitInput[] = input.split(":\\.*");
          if (splitInput.length <= 2) {
            portNumber = Integer.parseInt(splitInput[1]);
            serverName = splitInput[0];
          } else {
            portNumber = Integer.parseInt(splitInput[2]);
            serverName = splitInput[0] + ":" + splitInput[1];
          }
        }

        if (args.length > 1) {
          if (args[0].contains("IDS") || args[1].contains("IDS")) {
            stationID = "?stationID=" + args[1];
          } else {
            System.out.println(
              "(Invalid Station ID, using default)\n=====================\n"
            );
          }
        }

        try {
          InetAddress ip = InetAddress.getByName(serverName);
          serverName = ip.getHostAddress();
        } catch (UnknownHostException e) {
          System.err.println("Cannot find host.\n---");
          e.printStackTrace();
        }
      }
    } catch (Exception e) {
      System.err.println("Bad input.");
      e.printStackTrace();
    }

    // // Connect to server
    // client.connect(serverName, portNumber);

    String res = client.run(client, serverName, portNumber, stationID);

    if (res != "") {
      // Print response
      System.out.println(res);
      client.disconnect();
    }
  }
}
