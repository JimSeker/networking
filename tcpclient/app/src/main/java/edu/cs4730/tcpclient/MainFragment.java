package edu.cs4730.tcpclient;


import android.os.Bundle;
import android.os.Handler;
import android.os.Message;

import androidx.fragment.app.Fragment;

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
import java.net.InetAddress;
import java.net.Socket;

/**
 * this is a simple network client example.  Note it assumes you are using the emulators, but will work
 * on phones as well.  You just need to know the IP address.
 */

public class MainFragment extends Fragment implements Button.OnClickListener {
    TextView output;
    Button mkconn;
    EditText hostname, port;
    Thread myNet;

    public MainFragment() {
        // Required empty public constructor
    }

    @Override
    public View onCreateView(LayoutInflater inflater, ViewGroup container,
                             Bundle savedInstanceState) {
        // Inflate the layout for this fragment
        View myView = inflater.inflate(R.layout.fragment_main, container, false);
        output = myView.findViewById(R.id.output);
        output.append("\n");
        hostname = myView.findViewById(R.id.EThostname);
        //hostname.setText("10.0.2.2"); //This address is the localhost for the computer the emulator is running on.
        hostname.setText("10.121.174.200");
        port = myView.findViewById(R.id.ETport);
        mkconn = myView.findViewById(R.id.makeconn);
        mkconn.setOnClickListener(this);
        return myView;
    }

    @Override
    public void onClick(View v) {
        doNetwork stuff = new doNetwork();
        myNet = new Thread(stuff);
        myNet.start();

        //An example of how you would write from here via the thread.  Note,
        //this will likely force close here, because the connection is not fully made at this point.
        //the thread just started.
        //stuff.out.println("hi there.");

    }

    private Handler handler = new Handler(new Handler.Callback() {
        @Override
        public boolean handleMessage(Message msg) {
            output.append(msg.getData().getString("msg"));
            return true;
        }

    });

    public void mkmsg(String str) {
        //handler junk, because thread can't update screen!
        Message msg = new Message();
        Bundle b = new Bundle();
        b.putString("msg", str);
        msg.setData(b);
        handler.sendMessage(msg);
    }

    /*
     * this code does most of the work in a thread, so that it doesn't lock up the activity_main (UI) thread
     * It call mkmsg (which calls the handler to update the screen)
     */
    class doNetwork implements Runnable {
        public PrintWriter out;
        public BufferedReader in;

        public void run() {
            int p = Integer.parseInt(port.getText().toString());
            String h = hostname.getText().toString();
            mkmsg("host is " + h + "\n");
            mkmsg(" Port is " + p + "\n");
            try {
                InetAddress serverAddr = InetAddress.getByName(h);
                mkmsg("Attempt Connecting..." + h + "\n");
                Socket socket = new Socket(serverAddr, p);
                String message = "Hello from Client android emulator";

                //made connection, setup the read (in) and write (out)
                out = new PrintWriter(new BufferedWriter(new OutputStreamWriter(socket.getOutputStream())), true);
                in = new BufferedReader(new InputStreamReader(socket.getInputStream()));

                //now send a message to the server and then read back the response.
                try {
                    //write a message to the server
                    mkmsg("Attempting to send message ...\n");
                    out.println(message);
                    mkmsg("Message sent...\n");

                    //read back a message from the server.
                    mkmsg("Attempting to receive a message ...\n");
                    String str = in.readLine();
                    mkmsg("received a message:\n" + str + "\n");

                    mkmsg("We are done, closing connection\n");
                } catch (Exception e) {
                    mkmsg("Error happened sending/receiving\n");

                } finally {
                    in.close();
                    out.close();
                    socket.close();
                }

            } catch (Exception e) {
                mkmsg("Unable to connect...\n");
            }
        }
    }
}
