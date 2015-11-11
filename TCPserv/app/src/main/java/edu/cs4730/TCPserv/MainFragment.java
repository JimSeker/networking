package edu.cs4730.tcpserv;

import android.app.Service;
import android.net.wifi.WifiManager;
import android.os.Bundle;
import android.os.Handler;
import android.os.Message;
import android.support.v4.app.Fragment;
import android.text.format.Formatter;
import android.util.Log;
import android.view.LayoutInflater;
import android.view.View;
import android.view.ViewGroup;
import android.widget.Button;
import android.widget.EditText;
import android.widget.TextView;

import java.io.BufferedReader;
import java.io.BufferedWriter;
import java.io.InputStreamReader;
import java.io.OutputStreamWriter;
import java.io.PrintWriter;
import java.net.ServerSocket;
import java.net.Socket;

/* This a simple network server code.  The user must push the make connection button to setup the
* server to accept connections.
*
* Note use adb forward tcp:3012 tcp:3012
* to setup the emulator to receive.
*/
public class MainFragment extends Fragment implements Button.OnClickListener {
    TextView output;
    Button mkconn;
    EditText port;
    Thread myNet;

    String TAG = "TCPserv";

    public MainFragment() {
        // Required empty public constructor
    }


    @Override
    public View onCreateView(LayoutInflater inflater, ViewGroup container,
                             Bundle savedInstanceState) {
        // Inflate the layout for this fragment
        View myView = inflater.inflate(R.layout.fragment_main, container, false);
        output = (TextView) myView.findViewById(R.id.output);
        output.append("\n\n");
        port = (EditText) myView.findViewById(R.id.ETport);
        mkconn = (Button) myView.findViewById(R.id.makeconn);
        mkconn.setOnClickListener(this);

        //What is our IP address?   Fails on Marshmallow....
        WifiManager wm = (WifiManager) getActivity().getSystemService(Service.WIFI_SERVICE);
        String ip = Formatter.formatIpAddress(wm.getConnectionInfo().getIpAddress());
        output.append("Server IP address is " + ip);
        return myView;
    }


    private Handler handler = new Handler(new Handler.Callback() {
        @Override
        public boolean handleMessage(Message msg) {
            output.append(msg.getData().getString("msg"));
            return true;
        }

    });

    @Override
    public void onClick(View v) {
        //this way creates the thread annonymously.  quick and dirty, but generally a bad idea.
        //new Thread(new doNetwork()).start();

        //better way is this way, where we have access to the thread variable.
        doNetwork stuff = new doNetwork();
        myNet = new Thread(stuff);
        myNet.start();

    }


    public void mkmsg(String str) {
        //handler junk, because threads can't update screen!
        Message msg = new Message();
        Bundle b = new Bundle();
        b.putString("msg", str);
        msg.setData(b);
        handler.sendMessage(msg);
    }

    /*
     * Most of the work is done here, so it does not lock the UI thread.  Calls the handler to
     * do the screen updates.
     *
     * protocol for the example is the server reads in a line and then sends a line.
     * then it closes the connection.
     *
     */
    class doNetwork implements Runnable {
        public void run() {

            int p = Integer.parseInt(port.getText().toString());
            mkmsg(" Port is " + p + "\n");

            try {
                mkmsg("Waiting on Connecting...\n");
                Log.v(TAG,"S: Connecting...");
                ServerSocket serverSocket = new ServerSocket(p);

                //socket created, now wait for a coonection via accept.
                Socket client = serverSocket.accept();
                Log.v(TAG,"S: Receiving...");

                try {
                    //setup send/receive streams.
                    BufferedReader in = new BufferedReader(new InputStreamReader(client.getInputStream()));
                    PrintWriter out = new PrintWriter(new BufferedWriter(new OutputStreamWriter(client.getOutputStream())), true);

                    //receive the message first.
                    mkmsg("Attempting to receive a message ...\n");
                    String str = in.readLine();
                    mkmsg("received a message:\n" + str + "\n");

                    //now send a message.
                    String message = "Hello from server android emulator";
                    mkmsg("Attempting to send message ...\n");
                    out.println(message);
                    mkmsg("Message sent...\n");

                    //now close down the send/receive streams.
                    in.close();
                    out.close();

                } catch (Exception e) {
                    mkmsg("Error happened sending/receiving\n");

                } finally {
                    mkmsg("We are done, closing connection\n");
                    client.close();  //close the client connection
                    serverSocket.close();  //finally close down the server side as well.
                }

            } catch (Exception e) {
                mkmsg("Unable to connect...\n");
            }

        }
    }

}
