package edu.cs4730.httpurlcondemoasync;

import android.os.AsyncTask;
import android.os.Bundle;
import android.security.NetworkSecurityPolicy;
import android.view.View;
import android.webkit.URLUtil;
import android.widget.Button;

import java.io.BufferedReader;
import java.io.IOException;
import java.io.InputStream;
import java.io.InputStreamReader;
import java.net.HttpURLConnection;
import java.net.URI;
import java.net.URISyntaxException;
import java.net.URL;

import javax.net.ssl.HttpsURLConnection;

import androidx.appcompat.app.AppCompatActivity;
import androidx.core.graphics.Insets;
import androidx.core.view.ViewCompat;
import androidx.core.view.WindowInsetsCompat;

import edu.cs4730.httpurlcondemoasync.databinding.ActivityMainBinding;

/**
 * Example using a Async task to get a web page.
 * <p>
 * Note, https://koz.io/android-m-and-the-war-on-cleartext-traffic/
 * In the AndroidManifest.xml there is < application ... android:usesCleartextTraffic="true" ...
 * The test server doesn't have a legit cert, so... @#$@ it, cleartext it is.
 * For real app, with legit certs on web servers, you should use https and remove the above.
 */

public class MainActivity extends AppCompatActivity implements Button.OnClickListener {
    ActivityMainBinding binding;


    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        binding = ActivityMainBinding.inflate(getLayoutInflater());
        setContentView(binding.getRoot());
        ViewCompat.setOnApplyWindowInsetsListener(binding.main, (v, insets) -> {
            Insets systemBars = insets.getInsets(WindowInsetsCompat.Type.systemBars());
            v.setPadding(systemBars.left, systemBars.top, systemBars.right, systemBars.bottom);
            return WindowInsetsCompat.CONSUMED;
        });
        binding.makeconn.setOnClickListener(this);

        binding.output.append("\n");
        if (NetworkSecurityPolicy.getInstance().isCleartextTrafficPermitted()) {
            binding.output.append("Clear Text traffic is allowed.\n");
        } else {
            binding.output.append("Clear Text traffic is NOT allowed.\n");
        }

    }


    @Override
    public void onClick(View v) {
        URI url1;
        try {  //try catch is for the URI, not the asynctask, but if URI fails, no point.
            //url1 = new URI("http://www.cs.uwyo.edu/~seker/courses/4730/");   //not https!
            url1 = new URI("https://www.cs.uwyo.edu/~seker/courses/4730/");
            // url1 = new URI("https://www.google.com/");
            new doNetwork().execute(url1);
        } catch (URISyntaxException e) {
            binding.output.append("URI method failed?!  ");
            //e.printStackTrace();
        }
    }


    /**
     * Shows how to use an AsyncTask with a HttpClient method.
     */
    class doNetwork extends AsyncTask<URI, String, String> {

        /**
         * while this could have been in the doInBackground, I reused the
         * method already created the thread class.
         * <p>
         * This downloads a text file and returns it to doInBackground.
         */
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


        @Override
        protected String doInBackground(URI... params) {
            String page = "";

            publishProgress("Attempting to retrieve web page ...\n");
            try {
                //URL url = new URL(params[0].toString()); //but next line is much better! convert directly.
                URL url = params[0].toURL();
                publishProgress("address: " + url.toString() + "\n");
                if (URLUtil.isHttpsUrl(url.toString())) {
                    HttpsURLConnection con = (HttpsURLConnection) url.openConnection();
                    publishProgress("Connection made, reading in page.\n");
                    page = readStream(con.getInputStream());
                    publishProgress("Processed page:\n");
                    publishProgress(page);
                    publishProgress("Finished\n");
                    con.disconnect();
                } else {
                    //for HTTP, not https
                    HttpURLConnection con = (HttpURLConnection) url.openConnection();
                    publishProgress("Connection made, reading in page.\n");
                    page = readStream(con.getInputStream());
                    publishProgress("Processed page:\n");
                    publishProgress(page);
                    publishProgress("Finished\n");
                    con.disconnect();
                }

            } catch (Exception e) {
                publishProgress("Failed to retrieve web page ...\n");
                publishProgress(e.getMessage());

            }
            return page;  //return the page downloaded.
        }

        /**
         * This takes the place of handlers and my makemsg method, since we can directly access the screen.
         */
        protected void onProgressUpdate(String... progress) {
            binding.output.append(progress[0]);
        }

        /**
         * So the file has been downloaded and in this simple example it is displayed
         * to the screen.
         */
        protected void onPostExecute(String result) {
            binding.output.append(result);
        }
    }
}
