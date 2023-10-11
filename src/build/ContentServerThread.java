package build;

import java.io.BufferedReader;
import java.io.IOException;
import java.io.InputStreamReader;
import java.io.PrintWriter;
import java.net.Socket;
import java.net.UnknownHostException;

public class ContentServerThread extends Thread {

  private String serverName;
  private int portNumber;
  private Integer ID;
  private Socket contentSocket;
  private ContentServer content;
  private LamportClock lc = LamportClock.getInstance();
  private int csID;
  private PrintWriter out;
  private BufferedReader in;
  private String request;
  private Integer time;

  ContentServerThread(
    String serverName,
    int portNumber,
    Integer ID,
    ContentServer content,
    LamportClock lc,
    int csID,
    // PrintWriter out,
    // BufferedReader in,
    String request
  ) {
    this.serverName = serverName;
    this.portNumber = portNumber;
    this.ID = ID;
    this.content = content;
    this.lc = LamportClock.getInstance();
    this.csID = csID;
    this.request = request;
    // this.in = in;
    // this.out = out;
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
   * Disconnects and closes connection.
   */
  public void disconnect() throws IOException {
    this.in.close();
    this.out.close();
    this.contentSocket.close();
  }

  //   /**
  //    *  Content Server attempts to connect to the known Server.
  //    *  The client will try 3 times before giving up.
  //    *  Each attempt in 10 second intervals.
  //    */
  //   String start(
  //     ContentServer content,
  //     String serverName,
  //     int portNumber,

  //   ) throws UnknownHostException, IOException {
  //     String res = "";
  //     int attempts = 1;
  //     while (attempts <= 3) {
  //       try {
  //         content.connect(serverName, portNumber);
  //         res = content.sendMsg("GET /" + stationID + " HTTP/1.1");
  //         break;
  //       } catch (ConnectException ce) {
  //         if (attempts == 3) {
  //           System.err.println(
  //             "Attempt " + attempts + ": Could not connect. Try again later.\n---"
  //           );
  //           break;
  //         }
  //         System.err.println(
  //           "Attempt " +
  //           attempts +
  //           ": Could not connect, trying again in 10 seconds.\n---"
  //         );

  //         try {
  //           Thread.sleep(10000);
  //         } catch (InterruptedException ie) {
  //           ie.printStackTrace();
  //         }
  //       }
  //       attempts++;
  //     }
  //     return res;
  //   }

  /**
   * Send message to server.
   * Update Lamport Clock.
   * Returns response.
   */
  public synchronized String sendMsg(String msg) throws IOException {
    out.println(csID + "\r");
    out.println(this.time + "\r" + msg);

    String res = "";
    String line = in.readLine();
    while (true) {
      if (line == null) {
        break;
      }
      res += line + '\n';
      line = in.readLine();
    }
    // lc.receiveEvent(lc.getTime());
    return res;
  }

  /**
   * Will run the thread on start().
   * Reads and processes requests.
   * Sending a PUT request to the Agg Server.
   */
  @Override
  public synchronized void run() {
    String res;
    lc.sendEvent();
    this.time = lc.getTime();
    int attempts = 0;
    while (attempts < 10) {
      try {
        this.connect(serverName, portNumber);
        System.out.print("\n--- connected ---\n");
        System.out.print(
          "---\n" + this.ID + " entered the critical section.\n---\n"
        );
        System.out.println(this.request);
        res = this.sendMsg(this.request);

        if (
          res.contains("200 OK") ||
          res.contains("201 Created") ||
          res.contains("204 No Content") ||
          res.contains("400 Bad Request") ||
          res.contains("500 Internal Server Error")
        ) {
          System.out.println(res);
          lc.receiveEvent(time);
          System.out.print(
            "\n---\n" + this.ID + " Exiting the critical section.\n---\n\n"
          );
          // Reset attempts after success.
          attempts = 0;
          break;
        }
      } catch (IOException e) {
        System.err.println("Loop IO failure.\n");
        e.printStackTrace();
        attempts++;
        try {
          System.out.println(
            "\n!!!!!!\n" +
            csID +
            "NO RESPONSE: Attempting again in 10 seconds...\n!!!!!\n"
          );
          sleep(10000);
        } catch (InterruptedException se) {
          se.printStackTrace();
        }
      }
    }
    try {
      // Close the inputs and outputs when done.
      System.out.println("\n--- \nThread " + csID + " Closed\n---\n");
      in.close();
      out.close();
      this.interrupt();
    } catch (IOException e) {
      System.err.println("IO failure.\n");
      e.printStackTrace();
      this.interrupt();
    }
    this.interrupt();
  }
}
