package edu.cs4730.httpurlcondemothread;

import android.os.Bundle;
import android.os.Handler;
import android.os.Message;
import android.security.NetworkSecurityPolicy;
import android.view.View;
import android.widget.Button;
import android.widget.TextView;

import java.io.BufferedReader;
import java.io.IOException;
import java.io.InputStream;
import java.io.InputStreamReader;
import java.net.URL;

import javax.net.ssl.HttpsURLConnection;

import androidx.appcompat.app.AppCompatActivity;

/**
 * This get a web page via the URL connection with a thread.
 * <p>
 * Note, https://koz.io/android-m-and-the-war-on-cleartext-traffic/
 * In the AndroidManifest.xml there is < application ... android:usesCleartextTraffic="true" ...
 * The test server doesn't have a legit cert, so... @#$@ it, cleartext it is.
 * For real app, with legit certs on web servers, you should use https and remove the above.
 */

public class MainActivity extends AppCompatActivity implements Button.OnClickListener {
    TextView output;
    Button mkconn;

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_main);
        output = findViewById(R.id.output);
        output.append("\n");
        mkconn = findViewById(R.id.makeconn);
        mkconn.setOnClickListener(this);


        if (NetworkSecurityPolicy.getInstance().isCleartextTrafficPermitted()) {
            output.append("Clear Text traffic is allowed.\n");
        } else {
            output.append("Clear Text traffic is NOT allowed.\n");
        }

    }

    @Override
    public void onClick(View v) {
        //anonymous thread created here to get the webpage
        // All network must be done on a thread/async now or the OS will force the app.
        new Thread(new doNetwork()).start();

    }

    //handler which can update the screen and in this case show the html and messages.
    private Handler handler = new Handler(new Handler.Callback() {
        @Override
        public boolean handleMessage(Message msg) {
            output.append(msg.getData().getString("msg"));
            return true;
        }

    });

    /**
     * simple method to send messages to the handler.
     */
    public void mkmsg(String str) {
        //handler junk, because thread can't update screen!
        Message msg = new Message();
        Bundle b = new Bundle();
        b.putString("msg", str);
        msg.setData(b);
        handler.sendMessage(msg);
    }

    //Simple class that takes an InputStream and return the data
    //as a string, with line separators (ie end of line markers)
    private String readStream(InputStream in) {
        BufferedReader reader = null;
        StringBuilder sb = new StringBuilder("");
        String line = "";
        String NL = System.getProperty("line.separator");
        try {
            reader = new BufferedReader(new InputStreamReader(in));
            while ((line = reader.readLine()) != null) {
                sb.append(line).append(NL);
            }
        } catch (IOException e) {
            e.printStackTrace();
        } finally {
            if (reader != null) {
                try {
                    reader.close();
                } catch (IOException e) {
                    e.printStackTrace();
                }
            }
        }
        return sb.toString();
    }

    /**
     * actual thread class that calls executeHpptGet() above.
     */
    class doNetwork implements Runnable {
        public void run() {
            mkmsg("Attempting to retrieve web page ...\n");
            try {
                //for http, uncomment these two lines and comment out the https lines.
                //URL url = new URL("http://www.cs.uwyo.edu/~seker/courses/4730/");
                //HttpURLConnection con = (HttpURLConnection) url.openConnection();

                URL url = new URL("https://www.cs.uwyo.edu/~seker/courses/4730/");
                HttpsURLConnection con = (HttpsURLConnection) url.openConnection();

                mkmsg("Connection made, reading in page.\n");
                String page = readStream(con.getInputStream());
                mkmsg("Processed page:\n");
                mkmsg(page);
                mkmsg("Finished\n");
                con.disconnect();
            } catch (Exception e) {
                mkmsg("Failed to retrieve web page ...\n");
                mkmsg(e.getMessage());
                //e.printStackTrace();
            }
        }
    }

}
