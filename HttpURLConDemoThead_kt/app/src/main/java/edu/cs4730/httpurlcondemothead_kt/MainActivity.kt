package edu.cs4730.httpurlcondemothead_kt

import android.os.Bundle
import android.os.Handler
import android.os.Looper
import android.os.Message
import android.security.NetworkSecurityPolicy
import androidx.appcompat.app.AppCompatActivity
import androidx.core.view.ViewCompat
import androidx.core.view.WindowInsetsCompat
import edu.cs4730.httpurlcondemothead_kt.databinding.ActivityMainBinding
import java.io.BufferedReader
import java.io.IOException
import java.io.InputStream
import java.io.InputStreamReader
import java.net.HttpURLConnection
import java.net.URL
import javax.net.ssl.HttpsURLConnection

/**
 * This get a web page via the URL connection with a thread.
 * <p>
 * Note, https://koz.io/android-m-and-the-war-on-cleartext-traffic/
 * In the AndroidManifest.xml there is < application ... android:usesCleartextTraffic="true" ...
 * The test server doesn't have a legit cert, so... @#$@ it, cleartext it is. But our main server does have a cert to test with.
 * For real app, with legit certs on web servers, you should use https and remove the above.
 */


class MainActivity : AppCompatActivity() {

    lateinit var binding: ActivityMainBinding

    override fun onCreate(savedInstanceState: Bundle?) {
        super.onCreate(savedInstanceState)
        binding = ActivityMainBinding.inflate(layoutInflater)
        setContentView(binding.getRoot())
        ViewCompat.setOnApplyWindowInsetsListener(binding.main) { v, insets ->
            val systemBars = insets.getInsets(WindowInsetsCompat.Type.systemBars())
            v.setPadding(systemBars.left, systemBars.top, systemBars.right, systemBars.bottom)
            insets
        }

        binding.makeconn.setOnClickListener { Thread(doNetwork()).start() }

        binding.output.append("\n")
        if (NetworkSecurityPolicy.getInstance().isCleartextTrafficPermitted) {
            binding.output.append("Clear Text traffic is allowed.\n")
        } else {
            binding.output.append("Clear Text traffic is NOT allowed.\n")
        }
    }


    //handler which can update the screen and in this case show the html and messages.
    private val handler = Handler(Looper.getMainLooper()) { msg ->
        binding.output.append(msg.getData().getString("msg"))
        true
    }

    /**
     * simple method to send messages to the handler.
     */
    fun mkmsg(str: String) {
        //handler junk, because thread can't update screen!
        val msg = Message()
        val b = Bundle()
        b.putString("msg", str)
        msg.data = b
        handler.sendMessage(msg)
    }

    //Simple class that takes an InputStream and return the data
    //as a string, with line separators (ie end of line markers)
    private fun readStream(`in`: InputStream): String {
        var reader: BufferedReader? = null
        val sb = StringBuilder()
        var line: String? = ""
        val NL = System.lineSeparator()
        try {
            reader = BufferedReader(InputStreamReader(`in`))
            while ((reader.readLine().also { line = it }) != null) {
                sb.append(line).append(NL)
            }
        } catch (e: IOException) {
            e.printStackTrace()
        } finally {
            if (reader != null) {
                try {
                    reader.close()
                } catch (e: IOException) {
                    e.printStackTrace()
                }
            }
        }
        return sb.toString()
    }

    /**
     * actual thread class that calls readStream() above.
     */
    inner class doNetwork : Runnable {
        override fun run() {
            mkmsg("Attempting to retrieve web page ...\n")
            try {
                //for http, uncomment these two lines and comment out the https lines.
                //val url =  URL("http://www.cs.uwyo.edu/~seker/courses/4730/")
                //val con =  url.openConnection() as HttpURLConnection

                val url = URL("https://www.cs.uwyo.edu/~seker/courses/4730/")
                val con = url.openConnection() as HttpsURLConnection

                mkmsg("Connection made, reading in page.\n")
                val page: String = readStream(con.getInputStream())
                mkmsg("Processed page:\n")
                mkmsg(page)
                mkmsg("Finished\n")
                con.disconnect()
            } catch (e: Exception) {
                mkmsg("Failed to retrieve web page ...\n")
                mkmsg("msg: " + e.message)
                //e.printStackTrace();
            }
        }
    }


}