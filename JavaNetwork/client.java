import java.io.*;
import java.net.*;


public class client{
	public static BufferedReader stdIn;

	public static void main(String[] args){

	  mySocket s = new mySocket();
  	  String tempc;
  	  String array;
      stdIn = new BufferedReader( new InputStreamReader(System.in));

	  //System.out.print(args[0] + " " + args[1]);
	  // make connection to server
  	  s.MakeClientCon(args[0],Integer.parseInt(args[1]));
  	  System.out.println("Connection made.\n");

	  //wait for user input and follow directions
  	  while (true) {
		//read in from commandline ... have to find that command again...
		tempc = getLineS();
		if (tempc.equals("r")) {
			//read in from command line
			array = s.getLine();
			System.out.println("This was read: " + array);
	  	} else if (tempc.equals("w")) {
			System.out.print("What do you want to write? ");
		  	array = getLineS();
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
          // from = stdIn.readLine();
          	from = stdIn.readLine();
         } catch (IOException e) {
           from = "AWGH!!!";
         }
         return (from);
    }


}