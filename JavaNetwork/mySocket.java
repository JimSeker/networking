import java.io.*;
import java.net.*;

public class mySocket {
  public static Socket echoSocket = null;
  public static BufferedWriter out = null;
  public static BufferedReader in = null;
  public BufferedReader stdIn;


  /*  MakeClientCon  
      @Param  host    hostname to connect to like www.example.com
              port    port number of the server, like 3012
      Creates a socket and makes the connection with the host and port number
  */
  public void MakeClientCon(String host, int port){
  //     Making a connection to server, which is running on host at port
    try {
      echoSocket = new Socket(host, port);
      out = new BufferedWriter(new OutputStreamWriter(echoSocket.getOutputStream()));
      in = new BufferedReader(new InputStreamReader(echoSocket.getInputStream()));
    } catch (UnknownHostException e) {
      System.err.println("Don't know about host: localhost.");
      //System.exit(1);
    } catch (IOException e) {
      System.err.println("Couldn't get I/O for "
                         + "the connection to: localhost");
      //System.exit(1);
    }
  }

  /*  MakeServerCon
     @param  port   is the port number the server uses, like 3012
     Creates a server side socket and waitings for a connection.  This is a blocking function
     and will not return until a connection is made or fails.
  */
  public void MakeServerCon(int port) {
  //create a server socket and wait for connection..
    try {
      ServerSocket server = new ServerSocket (port);
      echoSocket = server.accept();
      server.close();
      //out = new PrintWriter(echoSocket.getOutputStream(), true);
      out = new BufferedWriter(new OutputStreamWriter(echoSocket.getOutputStream()));
      in = new BufferedReader(new InputStreamReader(echoSocket.getInputStream()));
    } catch ( IOException e) {
      System.err.println("Couldn't get I/O for "
                         + "the connection to: localhost");
      //System.exit(1);
    }
  }

  /* disconnect
     Closes the current socket.
  */
  public void disconnect () {
  // close down the socket
    try {
      in.close();
      out.close();
      echoSocket.close();
    } catch ( IOException e) {
      System.err.println("Can't close or already closed socket ");
      //System.exit(1);
    }
  }

  /* getLine  
    @param  returns a Strings
    getLine reads the socket (blocking read) and returns the a line of text as
    the return value.  If the socket fails to read, "AWGH!!!" is returned
  */
  public String getLine() {
    // read from network port and have default value
    String from;
    System.out.println("Read start");
    try {
      from = in.readLine();
    } catch (IOException e) {
      from = "AWGH!!!";
    }
    System.out.println("Read Done");
    return (from);
  }
 
  /*  writeLine
     @param   to  a string that to send via the socket
    Takes the variable to and sends to the receiving side of the socket
  */
  public void writeLine(String to) {
    System.out.println("Writing: "+to);
    //out.println(to);
    try {
    out.write(to,0,to.length());
    out.newLine();
    out.flush();
    } catch(IOException e) { System.out.println("Failed to write: "+e);}
    System.out.println("Writing done");
  }

}
