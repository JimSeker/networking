package edu.cs4730.tcpclient_kt

import android.os.Bundle
import android.os.Handler
import android.os.Looper
import android.os.Message
import android.view.View
import androidx.appcompat.app.AppCompatActivity
import edu.cs4730.tcpclient_kt.databinding.ActivityMainBinding
import java.io.BufferedReader
import java.io.BufferedWriter
import java.io.InputStreamReader
import java.io.OutputStreamWriter
import java.io.PrintWriter
import java.net.InetAddress
import java.net.Socket


/**
 * Note, https://koz.io/android-m-and-the-war-on-cleartext-traffic/
 * In the AndroidManifest.xml there is < application ... android:usesCleartextTraffic="true" ...
 * Actually this app didn't need it to work, but that is likely to change.
 *
 * this is a simple network client example.  Note it assumes you are using the emulators, but will work
 * on phones as well.  You just need to know the IP address.
 */
class MainActivity : AppCompatActivity() {
    lateinit var binding: ActivityMainBinding

    var myNet: Thread? = null
    override fun onCreate(savedInstanceState: Bundle?) {
        super.onCreate(savedInstanceState)
        binding = ActivityMainBinding.inflate(layoutInflater)
        setContentView(binding.root)

        binding.logger.append("\n")

        //This address is the localhost for the computer the emulator is running on.  If you are running
        //tcpserv in another emulator on the same machine, use this address
        binding.EThostname.setText("10.0.2.2")
        //This would be more running on the another phone or different host and likely not this ip address either.
        //hostname.setText("10.121.174.200");
        binding.makeconn.setOnClickListener(View.OnClickListener {
            val stuff: doNetwork = doNetwork()
            myNet = Thread(stuff)
            myNet!!.start()
        })
    }

    private val handler: Handler = object : Handler(Looper.getMainLooper()) {
        override fun handleMessage(msg: Message) {
            binding.logger.append(msg.data.getString("msg"))
        }
    }

    fun mkmsg(str: String?) {
        //handler junk, because thread can't update screen!
        val msg = Message()
        val b = Bundle()
        b.putString("msg", str)
        msg.data = b
        handler.sendMessage(msg)
    }

    /**
     * this code does most of the work in a thread, so that it doesn't lock up the activity_main (UI) thread
     * It call mkmsg (which calls the handler to update the screen)
     */
    internal inner class doNetwork : Runnable {
        lateinit var netOut: PrintWriter
        lateinit var netIn : BufferedReader  //in is a reserved word in kotlin.
        override fun run() {
            val p = binding.ETport.text.toString().toInt()
            val h = binding.EThostname.text.toString()
            mkmsg("host is $h\n")
            mkmsg(" Port is $p\n")
            try {
                val serverAddr = InetAddress.getByName(h)
                mkmsg("Attempt Connecting...$h\n")
                val socket = Socket(serverAddr, p)
                val message = "Hello from Client android emulator"

                //made connection, setup the read (in) and write (out)
                netOut = PrintWriter(BufferedWriter(OutputStreamWriter(socket.getOutputStream())), true)
                netIn = BufferedReader(InputStreamReader(socket.getInputStream()))

                //now send a message to the server and then read back the response.
                try {
                    //write a message to the server
                    mkmsg("Attempting to send message ...\n")
                    netOut.println(message)
                    mkmsg("Message sent...\n")

                    //read back a message from the server.
                    mkmsg("Attempting to receive a message ...\n")
                    val str = netIn.readLine()
                    mkmsg("received a message:\n$str\n")
                    mkmsg("We are done, closing connection\n")
                } catch (e: Exception) {
                    mkmsg("Error happened sending/receiving\n")
                } finally {
                    netIn.close()
                    netOut.close()
                    socket.close()
                }
            } catch (e: Exception) {
                mkmsg("Unable to connect...\n")
            }
        }
    }
}