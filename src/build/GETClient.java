/**
 * GETClient.java
 * [Description Here]
 */
package build;

import java.io.BufferedReader;
import java.io.IOException;
import java.io.InputStreamReader;
import java.io.PrintWriter;
import java.net.InetAddress;
import java.net.MalformedURLException;
import java.net.Socket;
import java.net.URI;
import java.net.URISyntaxException;
import java.net.URL;
import java.net.UnknownHostException;

public class GETClient {

  private Socket clientSocket;
  private PrintWriter out;
  private BufferedReader in;

  public GETClient() {}

  public void connect(String ip, int port)
    throws UnknownHostException, IOException {
    this.clientSocket = new Socket(ip, port);
    this.out = new PrintWriter(this.clientSocket.getOutputStream(), true);
    this.in =
      new BufferedReader(new InputStreamReader(clientSocket.getInputStream()));
  }

  public String sendMsg(String msg) throws IOException {
    out.println(msg);
    String res = in.readLine();
    return res;
  }

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
    } catch (Exception e) {
      e.printStackTrace();
    }

    client.connect(serverName, portNumber);
    System.out.println(client.sendMsg("GET"));
    System.out.println(client.sendMsg("Hello"));
    System.out.println(client.sendMsg("PUT"));

    client.disconnect();
  }
}
