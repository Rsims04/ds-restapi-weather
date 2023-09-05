/**
 * ContentServer.java
 * [Description Here]
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

public class ContentServer {

  private Socket contentSocket;
  private PrintWriter out;
  private BufferedReader in;
  public JSONParser jp = new JSONParser();

  public ContentServer() {}

  public void connect(String ip, int port)
    throws UnknownHostException, IOException {
    this.contentSocket = new Socket(ip, port);
    this.out = new PrintWriter(this.contentSocket.getOutputStream(), true);
    this.in =
      new BufferedReader(new InputStreamReader(contentSocket.getInputStream()));
  }

  public String sendMsg(String msg) throws IOException {
    out.println(msg);
    String res = in.readLine();
    return res;
  }

  public void disconnect() throws IOException {
    this.in.close();
    this.out.close();
    this.contentSocket.close();
  }

  public static void main(String[] args)
    throws UnknownHostException, IOException {
    ContentServer content = new ContentServer();
    String serverName = "localhost";
    int portNumber = 4567;

    try {
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
      String filePath = args[1];
      File file = new File(filePath);
      String jsonObject = content.jp.toJSON(file);
      // System.out.println(jsonObject);

      // Craft Header
      // PUT /weather.json HTTP/1.1
      // User-Agent: ATOMClient/1/0
      // Content-Type: (You should work this one out) application/json
      // Content-Length: (And this one too)
      String header = "PUT /" + filePath + " HTTP/1.1\n";
      String userAgent = "ATOMClient/1/0\n";
      String contentType = "application/json\n";
      String contentLength = Integer.toString(jsonObject.length()) + "\n";

      header += "User-Agent: " + userAgent;
      header += "Content-Type: : " + contentType;
      header += "Content-Length: : " + contentLength;
      header += "\n";

      String upload = header + jsonObject;

      System.out.println(upload);
      // Upload to server via PUT request

    } catch (Exception e) {
      e.printStackTrace();
    }

    content.connect(serverName, portNumber);
    System.out.println(content.sendMsg("PUT /weather.json HTTP/1.1"));

    content.disconnect();
  }

  {}
}
