package edu.cs4730.tcpdemo;

import android.app.Service;
import android.net.wifi.WifiManager;
import android.os.Bundle;

import androidx.annotation.NonNull;
import androidx.fragment.app.Fragment;

import android.text.format.Formatter;
import android.util.Log;
import android.view.LayoutInflater;
import android.view.View;
import android.view.ViewGroup;

import java.io.BufferedReader;
import java.io.BufferedWriter;
import java.io.InputStreamReader;
import java.io.OutputStreamWriter;
import java.io.PrintWriter;
import java.net.ServerSocket;
import java.net.Socket;

import edu.cs4730.tcpdemo.databinding.FragmentServerBinding;
import kotlin.Suppress;


public class ServerFragment extends Fragment {

    FragmentServerBinding binding;
    Thread myNet;
    String TAG = "TCPserv";

    public ServerFragment() {
        // Required empty public constructor
    }



    @Override
    public View onCreateView(@NonNull LayoutInflater inflater, ViewGroup container,
                             Bundle savedInstanceState) {
        // Inflate the layout for this fragment
        binding = FragmentServerBinding.inflate(inflater, container, false);

        binding.logger.append("\n\n");
        binding.makeconn.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View view) {
                //this way creates the thread anonymously.  quick and dirty, but generally a bad idea.
                //  new Thread(new doNetwork()).start();

                //better way is this way, where we have access to the thread variable.
                doNetwork stuff = new doNetwork();
                myNet = new Thread(stuff);
                myNet.start();
            }
        });

        //What is our IP address?
        WifiManager wm = (WifiManager) requireActivity().getSystemService(Service.WIFI_SERVICE);
        //noinspection deprecation    wifi can't return a ipv6, which is what the issue is, formater doesn't support ipv6
        @Suppress(names = "DEPRECATION")
        String ip = Formatter.formatIpAddress(wm.getConnectionInfo().getIpAddress());
        binding.logger.append("Server IP address is " + ip + "\n");

        return binding.getRoot();
    }


    /**
     * simple helper method to update the logger/output textview.
     */
    public void mkmsg(String str) {
        //so the client uses a handler, while the server code is going to use runUI method.
        //the method name makes no sense here, but keeping the name, so it matches the tpcclient.
        requireActivity().runOnUiThread(new Runnable() {
            @Override
            public void run() {
                binding.logger.append(str);
            }
        });
    }

    /**
     * Most of the work is done here, so it does not lock the UI thread.  Calls mkmsg, which run on the main thread to do updates.
     * <p>
     * protocol for the example is the server reads in a line and then sends a line.
     * then it closes the connection.
     */
    private class doNetwork implements Runnable {
        public void run() {

            int p = Integer.parseInt(binding.ETport.getText().toString());
            mkmsg(" Port is " + p + "\n");

            try {
                mkmsg("Waiting on Connecting...\n");
                Log.v(TAG, "S: Connecting...");
                ServerSocket serverSocket = new ServerSocket(p);

                //socket created, now wait for a coonection via accept.
                Socket client = serverSocket.accept();
                Log.v(TAG, "S: Receiving...");

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
                e.printStackTrace();
            }
        }
    }
}