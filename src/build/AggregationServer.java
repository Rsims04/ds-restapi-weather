/**
 * AggregationServer.java
 * [Description Here]
 */
package build;

import java.io.*;
import java.net.*;
import java.util.Date;

public class AggregationServer {

  private ServerSocket serverSocket;
  private Socket clientSocket;

  public AggregationServer() {}

  public void start(int port) throws IOException {
    this.clientSocket = null;
    this.serverSocket = new ServerSocket(port);
    System.out.println("Server listening on port: " + port);
    while (true) {
      try {
        this.clientSocket = this.serverSocket.accept();

        System.out.println("Client Connected!");
        BufferedReader in = new BufferedReader(
          new InputStreamReader(clientSocket.getInputStream())
        );
        PrintWriter out = new PrintWriter(clientSocket.getOutputStream(), true);

        System.out.println("New Thread ..");
        Thread t = new ServerThread(clientSocket, in, out);

        t.start();
      } catch (IOException e) {
        clientSocket.close();
        e.printStackTrace();
      }
    }
  }

  public static void main(String[] args) throws IOException {
    AggregationServer server = new AggregationServer();
    server.start(8080);
  }
}

class ServerThread extends Thread {

  private Socket clientSocket;
  private BufferedReader in;
  private PrintWriter out;

  /**
   * Constructor
   */
  public ServerThread(Socket clientSocket, BufferedReader in, PrintWriter out) {
    this.clientSocket = clientSocket;
    this.in = in;
    this.out = out;
  }

  @Override
  public void run() {
    while (true) {
      String line;
      try {
        PrintWriter writer = new PrintWriter(out, true);
        line = in.readLine();

        System.out.println(line);
        if (line.equals("GET")) {
          writer.println("200 OK; :)");
        } else {
          writer.println(new Date().toString());
          break;
        }
      } catch (IOException e) {
        e.printStackTrace();
      }
    }
    try {
      in.close();
      out.close();
    } catch (IOException e) {
      e.printStackTrace();
    }
  }
}
