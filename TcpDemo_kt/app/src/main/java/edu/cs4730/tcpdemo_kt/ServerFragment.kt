package edu.cs4730.tcpdemo_kt

import android.app.Service
import android.net.wifi.WifiManager
import android.os.Bundle
import android.text.format.Formatter
import android.util.Log
import android.view.LayoutInflater
import android.view.View
import android.view.ViewGroup
import androidx.fragment.app.Fragment
import edu.cs4730.tcpdemo_kt.databinding.FragmentServerBinding
import java.io.BufferedReader
import java.io.BufferedWriter
import java.io.InputStreamReader
import java.io.OutputStreamWriter
import java.io.PrintWriter
import java.net.ServerSocket


class ServerFragment : Fragment() {
    lateinit var binding: FragmentServerBinding
    var myNet: Thread? = null
    var TAG: String = "TCPserv"

    override fun onCreateView(
        inflater: LayoutInflater, container: ViewGroup?, savedInstanceState: Bundle?
    ): View {
        // Inflate the layout for this fragment
        binding = FragmentServerBinding.inflate(inflater, container, false)

        binding.logger.append("\n\n")
        binding.makeconn.setOnClickListener { //this way creates the thread anonymously.  quick and dirty, but generally a bad idea.
            //  new Thread(new doNetwork()).start();
            //better way is this way, where we have access to the thread variable.
            val stuff: doNetwork = doNetwork()
            myNet = Thread(stuff)
            myNet!!.start()
        }

        //What is our IP address?
        val wm = requireActivity().getSystemService(Service.WIFI_SERVICE) as WifiManager
        @Suppress("DEPRECATION")
        val ip = Formatter.formatIpAddress(wm.connectionInfo.ipAddress)
        binding.logger.append("Server IP address is $ip\n")

        return binding.root
    }


    /**
     * simple helper method to update the logger/output textview.
     */
    fun mkmsg(str: String?) {
        //so the client uses a handler, while the server code is going to use runUI method.
        //the method name makes no sense here, but keeping the name, so it matches the client code
        requireActivity().runOnUiThread { binding.logger.append(str) }
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
                    val netIn = BufferedReader(InputStreamReader(client.getInputStream()))
                    val netOut = PrintWriter(
                        BufferedWriter(OutputStreamWriter(client.getOutputStream())), true
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