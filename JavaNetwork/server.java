import java.io.*;
import java.net.*;


public class server{
	public static BufferedReader stdIn;

	public static void main(String[] args){

	  mySocket s = new mySocket();
  	  String tempc;
  	  String array;
      stdIn = new BufferedReader( new InputStreamReader(System.in));

	  //System.out.print(args[0] + " " + args[1]);
	  // make a server socket
	  if (args[0] =="") {args[0] = "3012";}
	  System.out.println("Waiting for connection on port "+args[0]);
  	  s.MakeServerCon(Integer.parseInt(args[0]));
  	  System.out.println("Connection made.\n");

	  //wait for user input and follow directions
  	  while (true) {
		//read in from commandline ... have to find that command again...
		System.out.print("Enter r to read, w to write, q to quit: " );
		tempc = getLineS();
		if (tempc.equals("r")) {
			//read in from command line
			array = s.getLine();
			System.out.println("This was read: " + array);
	  	} else if (tempc.equals("w")) {
			System.out.print("What do you want to write? ");
		  	array = getLineS();
			//array += "\n";
		  	s.writeLine(array);
	  	} else if (tempc.equals("q")) {
			s.disconnect();
			System.exit(0);
		}
	  }
	}


    public static String getLineS() {
    // read from command line and have default value

       String from;
         try {
           from = stdIn.readLine();
         } catch (IOException e) {
           from = "AWGH!!!";
         }
         return (from);
    }


}
