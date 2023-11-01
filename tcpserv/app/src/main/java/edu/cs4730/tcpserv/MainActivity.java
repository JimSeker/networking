package edu.cs4730.tcpserv;

import androidx.appcompat.app.AppCompatActivity;

import android.app.Service;
import android.net.wifi.WifiManager;
import android.os.Bundle;
import android.os.Handler;
import android.os.Message;
import android.text.format.Formatter;
import android.util.Log;
import android.view.View;
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

import edu.cs4730.tcpserv.databinding.ActivityMainBinding;
import kotlin.Suppress;

/**
 * Note, https://koz.io/android-m-and-the-war-on-cleartext-traffic/
 * In the AndroidManifest.xml there is < application ... android:usesCleartextTraffic="true" ...
 * Actually this app didn't need it to work, but that is likely to change.
 * <p>
 * If you are running this in emulators, make sure to run
 * adb forward tcp:3012 tcp:3012  for the server.
 * Do this on the first emulator, then launch the second one and run the client on the second emulator.
 * This assumes port 3012, so if you changed it, then change adb line as well.
 * <p>
 * Note, that while the IP address of the server is listed, if you are running on emulator use the host
 * address in tcpclient ip 10.0.2.2 (this is the address used by android for the host system.
 * <p>
 * If you want to talk the server from program like telnet, or whatever.  localhost: 3012  is the address
 * <p>
 * This a simple network server code.  The user must push the make connection button to setup the
 * server to accept connections.
 * <p>
 * Note use adb forward tcp:3012 tcp:3012
 * to setup the emulator to receive.
 */

public class MainActivity extends AppCompatActivity {

    ActivityMainBinding binding;
    Thread myNet;
    String TAG = "TCPserv";

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        binding = ActivityMainBinding.inflate(getLayoutInflater());
        setContentView(binding.getRoot());

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
        WifiManager wm = (WifiManager) getSystemService(Service.WIFI_SERVICE);
        //noinspection deprecation    wifi can't return a ipv6, which is what the issue is, formater doesn't support ipv6
        @Suppress(names = "DEPRECATION")
        String ip = Formatter.formatIpAddress(wm.getConnectionInfo().getIpAddress());
        binding.logger.append("Server IP address is " + ip + "\n");
    }

    /**
     * simple helper method to update the logger/output textview.
     */
    public void mkmsg(String str) {
        //so the client uses a handler, while the server code is going to use runUI method.
        //the method name makes no sense here, but keeping the name, so it matches the tpcclient.
        runOnUiThread(new Runnable() {
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
