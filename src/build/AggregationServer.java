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
  private BufferedReader in;
  private PrintWriter out;

  private LocalStorage localStorage = new LocalStorage();
  private PriorityQueue<Request> queue = new PriorityQueue<Request>();
  private LamportClock lc = LamportClock.getInstance();

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
   * Extracts and returns the content servers ID.
   * null if GET request.
   */
  public Integer extractTime(BufferedReader in) throws IOException {
    in.mark(1);
    String s = in.readLine();
    if (s.contains("GET")) {
      in.reset();
      return 0;
    }
    return Integer.parseInt(s);
  }

  /**
   * Creates a new thread and places in the queue
   */
  public void createThreadAndPlaceInQueue(
    String csID,
    int threadID,
    Integer time
  ) {
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
    Request r = new Request(time, t, threadID, csID);
    queue.add(r);
  }

  /**
   * Will start threads in the queue.
   * Order is determined by Lamport Clocks
   * and priority queue.
   * If request is a GET, order does not matter.
   */
  public void processQueue(Integer threadID) {
    while (!queue.isEmpty()) {
      try {
        Request request = queue.peek();
        // If request is not a GET, update clock.
        if (request.csID != null && request.clock != 0) {
          int serverTime = 0;
          int attempts = 0;
          while (attempts < 3) {
            try {
              serverTime = lc.getTime();
              lc.receiveEvent(serverTime);
              serverTime = lc.getTime();
              break;
            } catch (NumberFormatException e) {
              System.err.println("Attempting to update clock...");
            }
            attempts++;
          }
          // Lamport Clocks to determine order.
          if (request.clock < serverTime) {
            request = queue.remove();
            Thread.sleep(500);
            System.out.print("\n--- starting: " + request.threadID + " ---\n");
            request.thread.start();
            startTimer(request.csID, localStorage);
          }
        } else {
          request = queue.remove();
          request.thread.start();
        }
        request.thread.join();
      } catch (InterruptedException e) {
        System.err.println("Interrupt server failure.");
        e.printStackTrace();
      }
    }
  }

  /**
   * Start a 30 second timer.
   * After expired, removes all entries from provided csID in local storage.
   */
  public void startTimer(String csID, LocalStorage localStorage) {
    TimerTask task = new TimerTask() {
      public void run() {
        System.out.println(
          "Timeout: Entry " + csID + " Deleted at: " + new Date()
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
      System.out.println(
        "Local Storage exists: " + this.localStorage.getStore().getName()
      );
    }

    this.clientSocket = null;
    this.serverSocket = new ServerSocket(port);
    System.out.println("Server listening on port: " + port);
    while (true) {
      try {
        this.clientSocket = this.serverSocket.accept();

        this.in =
          new BufferedReader(
            new InputStreamReader(clientSocket.getInputStream())
          );
        this.out = new PrintWriter(clientSocket.getOutputStream(), true);

        int threadID = threadCount;
        String csID = extractID(in);
        if (csID != null) {
          csID += "-" + threadID;
        }
        Integer time = extractTime(in);

        // Create a new thread and place in the priority queue.
        createThreadAndPlaceInQueue(csID, threadID, time);

        // Print Queue.
        System.out.println("QUEUED:");
        if (!queue.isEmpty()) {
          for (Request request : queue) {
            System.err.println(
              "request: " + request.csID + " | clock: " + request.clock
            );
          }

          // Start threads in the queue.
          processQueue(threadID);
        }
      } catch (IOException e) {
        clientSocket.close();
        System.err.println("IO server failure.");
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
      System.err.println("Connection server failure.");
      e.printStackTrace();
    }
  }
}
