/**
 * AggregationServer.java
 * [Description Here]
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

  public String extractID(BufferedReader in) throws IOException {
    in.mark(1);
    String s = in.readLine();
    System.err.println("S: " + s);
    // System.exit(0);
    if (s.contains("GET")) {
      in.reset();
      return null;
    }

    return s;
  }

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

  public void start(int port) throws IOException {
    // Does local storage exist?
    // if (this.localStorage.exists()) {
    //   localFile = this.localStorage.getStore();
    //   System.out.println("Local Storage exists: " + this.localFile.getName());
    //   localFile.close();
    // }

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
        System.out.println("\n---\nNew Thread - id:" + threadID + "\n---\n ");
        Thread t = new AggregationServerThread(
          csID,
          threadID,
          clientSocket,
          in,
          out,
          localStorage,
          localFile
        );
        threadCount++;

        // Place in Queue
        Request r = new Request(lc.getTime(), t, csID);
        queue.add(r);

        // Lamport Clocks to determine order
        if (!queue.isEmpty()) {
          for (Request request : queue) {
            System.err.println("request: " + request.clock);
          }
          Request request = queue.peek();
          lc.recieveEvent(lc.getTime());
          if (request.clock < lc.getTime()) {
            request = queue.remove();
            request.thread.start();
            startTimer(request.csID, localStorage);
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
      int port = 4567;
      if (args.length > 0) {
        port = Integer.parseInt(args[0]);
      }
      AggregationServer server = new AggregationServer();
      server.start(port);
    } catch (IOException e) {
      e.printStackTrace();
    }
  }
}

class Request implements Comparable<Request> {

  int clock;
  Thread thread;
  String csID;

  Request(int clock, Thread thread, String csID) {
    this.clock = clock;
    this.thread = thread;
    this.csID = csID;
  }

  @Override
  public int compareTo(Request other) {
    if (this.clock == other.clock) return 0; else if (
      this.clock > other.clock
    ) return 1; else return -1;
  }
}
