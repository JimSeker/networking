package edu.cs4730.httpurlcondemoasync;

import android.support.v4.app.Fragment;
import android.os.Bundle;
import android.os.AsyncTask;
import android.view.LayoutInflater;
import android.view.View;
import android.view.ViewGroup;
import android.widget.Button;
import android.widget.TextView;

import java.io.BufferedReader;
import java.io.IOException;
import java.io.InputStream;
import java.io.InputStreamReader;
import java.net.HttpURLConnection;
import java.net.URI;
import java.net.URISyntaxException;
import java.net.URL;

//Example using a Async task to get a web page.

public class MainFragment extends Fragment implements Button.OnClickListener {
    TextView output;
    Button mkconn;

    public MainFragment() {
    }

    @Override
    public View onCreateView(LayoutInflater inflater, ViewGroup container,
                             Bundle savedInstanceState) {
        View myView = inflater.inflate(R.layout.fragment_main, container, false);

        output = (TextView) myView.findViewById(R.id.output);
        output.append("\n");
        mkconn = (Button) myView.findViewById(R.id.makeconn);
        mkconn.setOnClickListener(this);
        return myView;
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
        //Simple class that takes an InputStream and return the data
        //as a string, with line sepratorss (ie end of line markers)
        private String readStream(InputStream in) {
            BufferedReader reader = null;
            StringBuilder sb = new StringBuilder("");
            String line = "";
            String NL = System.getProperty("line.separator");
            try {
                reader = new BufferedReader(new InputStreamReader(in));
                while ((line = reader.readLine()) != null) {
                    sb.append(line + NL);
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
                HttpURLConnection con = (HttpURLConnection) url.openConnection();
                publishProgress("Connection made, reading in page.\n");
                page = readStream(con.getInputStream());
                publishProgress("Processed page:\n");
                publishProgress(page);
                publishProgress("Finished\n");
                con.disconnect();
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
