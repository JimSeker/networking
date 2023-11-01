package edu.cs4730.tcpserv_kt

import android.net.wifi.WifiManager
import android.os.Bundle
import android.text.format.Formatter
import android.util.Log
import android.widget.Button
import android.widget.EditText
import android.widget.TextView
import androidx.appcompat.app.AppCompatActivity
import edu.cs4730.tcpserv_kt.databinding.ActivityMainBinding
import java.io.*
import java.net.ServerSocket

/**
 * Note, https://koz.io/android-m-and-the-war-on-cleartext-traffic/
 * In the AndroidManifest.xml there is < application ... android:usesCleartextTraffic="true" ...
 * Actually this app didn't need it to work, but that is likely to change.
 *
 * If you are running this in emulators, make sure to run
 * adb forward tcp:3012 tcp:3012  for the server.
 * Do this on the first emulator, then launch the second one and run the client on the second emulator.
 * This assumes port 3012, so if you changed it, then change adb line as well.
 *
 * Note, that while the IP address of the server is listed, if you are running on emulator use the host
 * address in tcpclient ip 10.0.2.2 (this is the address used by android for the host system.
 *
 * If you want to talk the server from program like telnet, or whatever.  localhost: 3012  is the address
 *
 * This a simple network server code.  The user must push the make connection button to setup the
 * server to accept connections.
 *
 * Note use adb forward tcp:3012 tcp:3012
 * to setup the emulator to receive.
 */
class MainActivity : AppCompatActivity() {
    lateinit var binding: ActivityMainBinding
    var myNet: Thread? = null
    var TAG = "TCPserv"

    override fun onCreate(savedInstanceState: Bundle?) {
        super.onCreate(savedInstanceState)
        binding = ActivityMainBinding.inflate(layoutInflater)
        setContentView(binding.root)

        binding.logger.append("\n\n")
        binding.makeconn.setOnClickListener {
            //this way creates the thread anonymously.  quick and dirty, but generally a bad idea.
            //Thread(doNetwork()).start()

            //better way is this way, where we have access to the thread variable.
            val stuff: doNetwork = doNetwork()
            myNet = Thread(stuff)
            myNet!!.start()
        }

        //What is our IP address?
        val wm = getSystemService(WIFI_SERVICE) as WifiManager
        //noinspection deprecation    wifi can't return a ipv6, which is what the issue is, formater doesn't support ipv6
        @Suppress("DEPRECATION")
        val ip = Formatter.formatIpAddress(wm.connectionInfo.ipAddress)
        binding.logger.append("Server IP address is $ip\n")
    }

    /**
     * simple hepler method to update the logger/output textview.
     */
    fun mkmsg(str: String) {
        //so the client uses a handler, while the server code is going to use runUI method.
        //the method name makes no sense here, but keeping the name, so it matches the tpcclient.
        runOnUiThread { binding.logger.append(str) }
    }

    /**
     * Most of the work is done here, so it does not lock the UI thread.  Calls mkmsg, which run on the main thread to do updates.
     *
     * protocol for the example is the server reads in a line and then sends a line.
     * then it closes the connection.
     */
    private inner class doNetwork : Runnable {
        override fun run() {
            val p = binding.ETport.text.toString().toInt()
            mkmsg(" Port is $p\n")
            try {
                mkmsg("Waiting on Connecting...\n")
                Log.v(TAG, "S: Connecting...")
                val serverSocket = ServerSocket(p)

                //socket created, now wait for a coonection via accept.
                val client = serverSocket.accept()
                Log.v(TAG, "S: Receiving...")
                try {
                    //setup send/receive streams.
                    val netIn =
                        BufferedReader(InputStreamReader(client.getInputStream()))  //in is a reserved word in kotlin.
                    val netOut = PrintWriter(
                        BufferedWriter(OutputStreamWriter(client.getOutputStream())),
                        true
                    )

                    //receive the message first.
                    mkmsg("Attempting to receive a message ...\n")
                    val str = netIn.readLine()
                    mkmsg("received a message:\n$str\n")

                    //now send a message.
                    val message = "Hello from server android emulator"
                    mkmsg("Attempting to send message ...\n")
                    netOut.println(message)
                    mkmsg("Message sent...\n")

                    //now close down the send/receive streams.
                    netIn.close()
                    netOut.close()
                } catch (e: Exception) {
                    mkmsg("Error happened sending/receiving\n")
                } finally {
                    mkmsg("We are done, closing connection\n")
                    client.close() //close the client connection
                    serverSocket.close() //finally close down the server side as well.
                }
            } catch (e: Exception) {
                mkmsg("Unable to connect...\n")
                e.printStackTrace()
            }
        }
    }
}