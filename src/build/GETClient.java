/**
 * GETClient.java
 * [Description Here]
 */
package build;

import java.io.BufferedReader;
import java.io.IOException;
import java.io.InputStream;
import java.io.InputStreamReader;
import java.io.PrintWriter;
import java.net.Socket;
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
    client.connect("127.0.0.1", 8080);
    System.out.println(client.sendMsg("GET"));
    System.out.println(client.sendMsg("Hello"));

    client.disconnect();
  }
}
