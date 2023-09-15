/**
 * AggregationServer.java
 * [Description Here]
 */
package build;

import java.io.*;
import java.net.*;

public class AggregationServer {

  private ServerSocket serverSocket;
  private Socket clientSocket;
  private LocalStorage localStorage = new LocalStorage();
  private File localFile;
  private int threadCount = 0;

  public AggregationServer() {}

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
        System.out.println("\n---\nNew Thread - id:" + threadID + "\n---\n ");
        Thread t = new AggregationServerThread(
          threadID,
          clientSocket,
          in,
          out,
          localStorage,
          localFile
        );
        threadCount++;
        t.start();
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
