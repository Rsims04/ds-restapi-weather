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

  private String threadID;
  private Socket contentSocket;
  private ContentServer content;
  private LamportClock lc = LamportClock.getInstance();
  private int csID;
  private PrintWriter out;
  private BufferedReader in;
  private String request;
  private Integer time;

  ContentServerThread(
    String threadID,
    String serverName,
    int portNumber,
    ContentServer content,
    LamportClock lc,
    int csID,
    String request
  ) {
    this.threadID = threadID;
    this.serverName = serverName;
    this.portNumber = portNumber;

    this.content = content;
    this.lc = LamportClock.getInstance();
    this.csID = csID;
    this.request = request;
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

  /**
   * Send message to server.
   * Update Lamport Clock.
   * Returns response.
   */
  public synchronized String sendMsg(String msg) throws IOException {
    out.println(threadID + "\r");
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

        System.out.println(this.request);
        res = this.sendMsg(this.request);

        if (res.contains("200 OK") || res.contains("201 Created")) {
          System.out.println(res);
          lc.receiveEvent(time);

          // Reset attempts after success.
          attempts = 0;
          break;
        } else if (
          res.contains("204 No Content") ||
          res.contains("400 Bad Request") ||
          res.contains("500 Internal Server Error")
        ) {
          attempts++;
          System.out.println(
            csID +
            " : attempt " +
            attempts +
            " - Attempting again in 10 seconds...\n---\n"
          );
          try {
            sleep(10000);
          } catch (InterruptedException e) {
            e.printStackTrace();
          }
          continue;
        }
      } catch (IOException e) {
        System.err.println("Server Failure.\n");
        attempts++;
        System.out.println(
          "\n!!!!!!\n" +
          csID +
          " NO RESPONSE: Attempting again in 10 seconds...\n!!!!!\n"
        );
        try {
          sleep(10000);
        } catch (InterruptedException se) {
          e.printStackTrace();
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
