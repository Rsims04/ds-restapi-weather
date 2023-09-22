/**
 * AggregationServer.java
 * The Aggregation Server will receive GET and PUT requests.
 * Place them in a priority queue using Lamport Clocks to determine order.
 * Then process each individual request in a new thread.
 * Also responsible for cleaning expired entries in local storage.
 */
package build;

import java.io.*;
import java.net.*;
import java.util.Date;
import java.util.PriorityQueue;
import java.util.Timer;
import java.util.TimerTask;

public class AggregationServer {

  private ServerSocket serverSocket;
  private Socket clientSocket;
  private LocalStorage localStorage = new LocalStorage();
  private PriorityQueue<Request> queue = new PriorityQueue<Request>();
  private File localFile;
  private LamportClock lc = new LamportClock();

  private int threadCount = 0;

  public AggregationServer() {}

  /**
   * Extracts and returns the content servers ID.
   * null if GET request.
   */
  public String extractID(BufferedReader in) throws IOException {
    in.mark(1);
    String s = in.readLine();
    if (s.contains("GET")) {
      in.reset();
      return null;
    }

    return s;
  }

  /**
   * Start a 30 second timer.
   * After expired, removes all entries from provided csID in local storage.
   */
  public void startTimer(String csID, LocalStorage localStorage) {
    TimerTask task = new TimerTask() {
      public void run() {
        System.out.println(
          "Task performed on: " + new Date() + ";" + "Thread's name: " + csID
        );
        try {
          localStorage.removeEntries(csID);
        } catch (IOException e) {
          e.printStackTrace();
        }
      }
    };
    Timer timer = new Timer("Timer");

    long delay = 30000;
    timer.schedule(task, delay);
  }

  /**
   * Listens on provided port for client connections.
   * Processes requests threads in queue ordered by Lamport Clocks.
   * Starts 30 second timer when request is received.
   */
  public void start(int port) throws IOException {
    // Does local storage exist?
    if (this.localStorage.exists()) {
      localFile = this.localStorage.getStore();
      System.out.println("Local Storage exists: " + this.localFile.getName());
    }

    this.clientSocket = null;
    this.serverSocket = new ServerSocket(port);
    System.out.println("Server listening on port: " + port);
    while (true) {
      try {
        this.clientSocket = this.serverSocket.accept();

        System.out.println("\nClient Connected!");
        BufferedReader in = new BufferedReader(
          new InputStreamReader(clientSocket.getInputStream())
        );
        PrintWriter out = new PrintWriter(clientSocket.getOutputStream(), true);

        int threadID = threadCount;
        String csID = extractID(in);
        if (csID != null) {
          csID += threadID;
        }
        System.out.println("\n---\nNew Thread - id:" + threadID + "\n---\n ");
        Thread t = new AggregationServerThread(
          csID,
          threadID,
          clientSocket,
          in,
          out,
          localStorage
        );
        threadCount++;

        // Place in Queue
        Request r = new Request(lc.getTime(), t, csID);
        queue.add(r);

        // Lamport Clocks to determine order
        if (!queue.isEmpty()) {
          for (Request request : queue) {
            System.err.println(
              "request: " + request.csID + "| clock: " + request.clock
            );
          }
          Request request = queue.peek();
          lc.receiveEvent(lc.getTime());
          if (request.clock < lc.getTime()) {
            request = queue.remove();
            request.thread.start();
            if (csID != null) {
              startTimer(request.csID, localStorage);
            }
          }
        }
      } catch (IOException e) {
        clientSocket.close();
        e.printStackTrace();
      }
    }
  }

  public static void main(String[] args) throws IOException {
    try {
      // Default port
      int port = 4567;
      if (args.length > 0) {
        // Command line provided port
        port = Integer.parseInt(args[0]);
      }
      AggregationServer server = new AggregationServer();
      // Start server
      server.start(port);
    } catch (IOException e) {
      e.printStackTrace();
    }
  }
}
