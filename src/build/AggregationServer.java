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
  private LocalStorage localStorage = new LocalStorage();
  private File localFile;

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

        System.out.println("New Thread ..\n---\n");
        Thread t = new ServerThread(
          clientSocket,
          in,
          out,
          localStorage,
          localFile
        );
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

class ServerThread extends Thread {

  private Socket clientSocket;
  private BufferedReader in;
  private PrintWriter out;
  private LocalStorage localStorage;
  private File localFile;

  /**
   * Constructor
   */
  public ServerThread(
    Socket clientSocket,
    BufferedReader in,
    PrintWriter out,
    LocalStorage localStorage,
    File localFile
  ) {
    this.clientSocket = clientSocket;
    this.in = in;
    this.out = out;
    this.localStorage = localStorage;
    this.localFile = localFile;
  }

  @Override
  public void run() {
    while (true) {
      String line;
      try {
        PrintWriter writer = new PrintWriter(out, true);
        line = in.readLine();

        if (line == null) {
          break;
        }

        System.out.println(line);

        if (line.equals("GET / HTTP/1.1")) {
          // Send Weather Data To Client
          writer.println("200 - OK");
          break;
        } else if (line.contains("PUT")) {
          if (this.localStorage.exists()) {
            this.localFile = this.localStorage.getStore();
          } else {
            this.localFile = this.localStorage.createStore();
          }

          // Do Content Server Stuff
          int contentLength = 0;
          // Read header - validate input
          while (!line.equals("")) {
            System.err.println(line);
            line = in.readLine();
            // Extract Content Length
            if (line.contains("Content-Length")) {
              contentLength = Integer.parseInt(line.replaceAll("\\D+", ""));
            }
          }
          line = in.readLine();

          if (line.contains("{")) {
            String jsonObject = "";
            int readLength = 0;
            while (readLength <= contentLength || line == null) {
              line += "\n";
              jsonObject += line;
              readLength += line.getBytes().length;

              if (readLength < contentLength) {
                line = in.readLine();
              }
            }
            System.err.println("\n" + jsonObject);

            JSONParser j = new JSONParser();
            System.err.print(j.validateJSON(jsonObject));

            if (j.validateJSON(jsonObject)) {
              writer.println("200 - OK");
              localStorage.updateStore(jsonObject);
            } else {
              writer.println("500 - Internal server error");
            }
          } else {
            writer.println("204 - No Content");
            break;
          }

          // Your server is designed to stay current
          // and will remove any items in the JSON
          // that have come from content servers which
          // it has not communicated with for 30 seconds

          // if storage file does not exist
          // - create it
          // - return status 201 - HTTP_CREATED

          // else if later uploads are successful
          // - return status 201 - succeed code

          // then before the content server lost connection,
          // - all other succeed response should use 200

          // Sending no content to the server
          // - status 204 -
          writer.println("Content Server PUT request...");
        } else {
          // Returns Status 400
          writer.println("400 - Bad Request");
          continue;
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
