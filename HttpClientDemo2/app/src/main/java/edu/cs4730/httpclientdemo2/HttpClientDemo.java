package edu.cs4730.httpclientdemo2;

import android.os.Bundle;
import android.os.AsyncTask;
import android.support.v7.app.ActionBarActivity;
import android.view.View;
import android.widget.Button;
import android.widget.TextView;

import java.io.BufferedReader;
import java.io.IOException;
import java.io.InputStreamReader;
import java.net.URI;
import java.net.URISyntaxException;

import org.apache.http.HttpResponse;
import org.apache.http.client.HttpClient;
import org.apache.http.client.methods.HttpGet;
import org.apache.http.impl.client.DefaultHttpClient;

public class HttpClientDemo extends ActionBarActivity implements Button.OnClickListener {
    TextView output;
    Button mkconn;

    @Override
    public void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.main);
        output = (TextView) findViewById(R.id.output);
        output.append("\n");
        mkconn = (Button) findViewById(R.id.makeconn);
        mkconn.setOnClickListener(this);
    }

    @Override
    public void onClick(View v) {
        URI url1;
        try {  //try catch is for the URI, not the asynctask, but if URI fails, no point.
            url1 = new URI("http://www.cs.uwyo.edu/~seker/courses/4730/index.html");
            new doNetwork().execute(url1);
        } catch (URISyntaxException e) {
            output.append("URI method failed?!  ");
            //e.printStackTrace();
        }
    }


    /*
     * Shows how to use an AsyncTask with a HttpClient method.
     */
    class doNetwork extends AsyncTask<URI, String, String> {

        /*
         * while this could have been in the doInBackground, I reused the
         * method already created the thread class.
         *
         * This downloads a text file and returns it to doInBackground.
         */
        public String executeHttpGet(URI url) throws Exception {
            BufferedReader in = null;
            try {
                HttpClient client = new DefaultHttpClient();
                HttpGet request = new HttpGet();
                request.setURI(url);
                publishProgress("Requesting web page.\n");
                HttpResponse response = client.execute(request);
                publishProgress("Web page received, processing it.\n");
                in = new BufferedReader(new InputStreamReader(response.getEntity().getContent()));
                StringBuffer sb = new StringBuffer("");
                String line = "";
                String NL = System.getProperty("line.separator");
                while ((line = in.readLine()) != null) {
                    sb.append(line + NL);
                }
                in.close();
                String page = sb.toString();
                publishProgress("Processed page:");
                return page;
            } finally {
                if (in != null) {
                    try {
                        in.close();
                    } catch (IOException e) {
                        e.printStackTrace();
                    }
                }

            }
        }

        @Override
        protected String doInBackground(URI... params) {
            String page = "";
            publishProgress("Attempting to retrieve web page ...\n");
            try {
                page = executeHttpGet(params[0]);
                publishProgress("Finished\n");
            } catch (Exception e) {
                publishProgress("Failed to retrieve web page ...\n");
                publishProgress(e.getMessage());

            }
            return page;  //return the page downloaded.
        }

        /*
         * This takes the place of handlers and my makemsg method, since we can directly access the screen.
         */
        protected void onProgressUpdate(String... progress) {
            output.append(progress[0]);
        }

        /*
         * So the file has been downloaded and in this simple example it is displayed
         * to the screen.
         */
        protected void onPostExecute(String result) {
            output.append(result);
        }


    }


}
