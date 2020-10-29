package edu.cs4730.restdemo2;

import android.os.AsyncTask;
import android.os.Bundle;

import com.google.android.material.floatingactionbutton.FloatingActionButton;

import androidx.swiperefreshlayout.widget.SwipeRefreshLayout;
import androidx.appcompat.app.AppCompatActivity;
import androidx.recyclerview.widget.DefaultItemAnimator;
import androidx.recyclerview.widget.LinearLayoutManager;
import androidx.recyclerview.widget.RecyclerView;
import androidx.appcompat.widget.Toolbar;
import androidx.recyclerview.widget.ItemTouchHelper;

import android.util.Log;
import android.view.View;
import android.widget.Toast;


import java.io.BufferedReader;
import java.io.BufferedWriter;
import java.io.IOException;
import java.io.InputStream;
import java.io.InputStreamReader;
import java.io.OutputStream;
import java.io.OutputStreamWriter;
import java.io.UnsupportedEncodingException;
import java.net.Authenticator;
import java.net.HttpURLConnection;
import java.net.PasswordAuthentication;
import java.net.URI;
import java.net.URISyntaxException;
import java.net.URL;
import java.net.URLEncoder;
import java.util.ArrayList;
import java.util.HashMap;
import java.util.Map;

/**
 * an example of custom rest service.  Note, the rest service here will only answer to ip from on UW's campus
 * The service has a query.php, insert.php, update.php, and delete.php
 * The data returned from query is in a csv format.  The other three return a number that states
 * how "objects" where changed.
 *
 * Authenticator info can be found here
 * http://docs.oracle.com/javase/6/docs/technotes/guides/net/http-auth.html  and
 * http://stackoverflow.com/questions/4883100/how-to-handle-http-authentication-using-httpurlconnection
 *
 * SECURITY NOTE:
 * https really should be used so the username and password are encrypted, but our website doesn't have a valid cert,
 * if yours does, change it to https://... * and HttpsURLconnection and everything else is the same.
 *
 * Note, https://koz.io/android-m-and-the-war-on-cleartext-traffic/
 * In the AndroidManifest.xml there is < application ... android:usesCleartextTraffic="true" ...
 * The test server doesn't have a legit cert, so... @#$@ it, cleartext it is.
 * For real app, with legit certs on web servers, you should use https and remove the above.
 */


public class MainActivity extends AppCompatActivity implements myDialogFragment.OnFragmentInteractionListener {
    String TAG = "MainActivity";
    RecyclerView mRecyclerView;
    myAdapter mAdapter;
    SwipeRefreshLayout mSwipeRefreshLayout;
    ArrayList<myObj> list = null;
    URI uri;


    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_main);
        Toolbar toolbar = findViewById(R.id.toolbar);
        setSupportActionBar(toolbar);

        try {
            uri = new URI("http://www.cs.uwyo.edu/~seker/rest/query.php");
        } catch (URISyntaxException e) {
            e.printStackTrace();
        }


        FloatingActionButton fab = findViewById(R.id.fab);
        fab.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View view) {
                //the user clicked the add button, so start the dialog with for add.
                myDialogFragment myDialog = myDialogFragment.newInstance(false, -1, "", "");
                myDialog.show(getSupportFragmentManager(), null);
            }
        });

        //setup the RecyclerView
        mRecyclerView = findViewById(R.id.list);
        mRecyclerView.setLayoutManager(new LinearLayoutManager(getApplicationContext()));
        mRecyclerView.setItemAnimator(new DefaultItemAnimator());
        //setup the adapter, which is myAdapter, see the code.  set it initially to null
        //use the asynctask to set the data later after it is loaded.
        mAdapter = new myAdapter(null, R.layout.rowlayout, getApplicationContext());
        mAdapter.setOnItemClickListener(new myAdapter.OnItemClickListener() {
            @Override
            public void onItemClick(String mid, String mtitle, String mbody) {
                myDialogFragment myDialog = myDialogFragment.newInstance(true, Integer.valueOf(mid), mtitle, mbody);
                myDialog.show(getSupportFragmentManager(), null);
            }
        });
        //add the adapter to the recyclerview
        mRecyclerView.setAdapter(mAdapter);


        //SwipeRefreshlayout setup.
        mSwipeRefreshLayout = findViewById(R.id.activity_main_swipe_refresh_layout);
        //setup some colors for the refresh circle.
        mSwipeRefreshLayout.setColorSchemeResources(R.color.orange, R.color.green, R.color.blue);
        //now setup the swiperefrestlayout listener where the main work is done.
        mSwipeRefreshLayout.setOnRefreshListener(new SwipeRefreshLayout.OnRefreshListener() {
            @Override
            public void onRefresh() {
                //where we call the refresher parts.
                doDataUpdate();  //refresh call, so reload the data from the internet.
            }
        });
        doDataUpdate();  //finally load the data via the internet.


        //setup left/right swipes on the cardviews for the delete.
        ItemTouchHelper.SimpleCallback simpleItemTouchCallback = new ItemTouchHelper.SimpleCallback(0, ItemTouchHelper.RIGHT) {
            @Override
            public boolean onMove(RecyclerView recyclerView, RecyclerView.ViewHolder viewHolder, RecyclerView.ViewHolder target) {
                //likely allows to for animations?  or moving items in the view I think.
                return false;
            }

            @Override
            public void onSwiped(RecyclerView.ViewHolder viewHolder, int direction) {
                //called when it has been animated off the screen.  So item is no longer showing.
                //use ItemtouchHelper.X to find the correct one.

                if (direction == ItemTouchHelper.RIGHT) {
                    //user wants to delet this entry.
                    int item = Integer.valueOf(((myAdapter.ViewHolder) viewHolder).tvID.getText().toString());
                    try {
                        //run async task to delete this data item.
                        new doRest().execute(new myDataAsync(new URI("http://www.cs.uwyo.edu/~seker/rest/delete.php"),
                            false, item, "", ""));

                    } catch (URISyntaxException e) {
                        e.printStackTrace();
                    }

                }
            }
        };
        ItemTouchHelper itemTouchHelper = new ItemTouchHelper(simpleItemTouchCallback);
        itemTouchHelper.attachToRecyclerView(mRecyclerView);
    }


    //help class to send all the data to the async task.  mostly because setting up all the
    //post data is just a pain in the but now.
    class myDataAsync {
        URI uri;
        boolean update;
        String data;

        //this code came from stackover  second answer
        //http://stackoverflow.com/questions/9767952/how-to-add-parameters-to-httpurlconnection-using-post
        private String getPostDataString(HashMap<String, String> params) throws UnsupportedEncodingException {
            StringBuilder result = new StringBuilder();
            boolean first = true;
            for (Map.Entry<String, String> entry : params.entrySet()) {
                if (first)
                    first = false;
                else
                    result.append("&");

                result.append(URLEncoder.encode(entry.getKey(), "UTF-8"));
                result.append("=");
                result.append(URLEncoder.encode(entry.getValue(), "UTF-8"));
            }

            return result.toString();
        }

        //constructor to create the data structure.
        myDataAsync(URI myuri, boolean update, int id, String title, String body) {
            this.update = update;
            uri = myuri;
            HashMap<String, String> hmap = new HashMap<String, String>();
            hmap.put("id", String.valueOf(id));
            hmap.put("title", title);
            hmap.put("body", body);
            try {
                data = getPostDataString(hmap);
            } catch (UnsupportedEncodingException e) {
                e.printStackTrace();
            }

        }
    }

    //simple method that is called by lots of different spots, to reload the query data.
    public void doDataUpdate() {
        mSwipeRefreshLayout.setRefreshing(true);
        //call the refresh code manually here.
        list = new ArrayList<myObj>();  //set the list.
        new doNetwork().execute(uri);
    }

    //fragment listener for update/add for myDialogFragment
    //then setup the data structure for either update or insert and call the asynctask to do the work.
    @Override
    public void onFragmentInteraction(Boolean update, int id, String title, String body) {
        //return from dialog, now save the data.
        Log.v("listener", "id " + id + title + body);
        myDataAsync myData;
        URI localuri = null;
        if (update) {
            //new doRest(new myDataAsync());
            try {
                localuri = new URI("http://www.cs.uwyo.edu/~seker/rest/update.php");
            } catch (URISyntaxException e) {
                e.printStackTrace();
            }

        } else {
            try {
                localuri = new URI("http://www.cs.uwyo.edu/~seker/rest/insert.php");
            } catch (URISyntaxException e) {
                e.printStackTrace();
            }
        }
        new doRest().execute(new myDataAsync(localuri, update, id, title, body));
    }

    //authenication code used from:
    //http://developer.android.com/reference/java/net/HttpURLConnection.html
    //and http://stackoverflow.com/questions/4883100/how-to-handle-http-authentication-using-httpurlconnection
    static final String kuser = "user1"; // your account name
    static final String kpass = "android"; // your password for the account

    private static class MyAuthenticator extends Authenticator {
        public PasswordAuthentication getPasswordAuthentication() {
            // I haven't checked getRequestingScheme() here, since for NTLM
            // and Negotiate, the usrname and password are all the same.
            System.err.println("Feeding username and password for " + getRequestingScheme());
            return (new PasswordAuthentication(kuser, kpass.toCharArray()));
        }
    }


    /*
     * uses an AsyncTask with a httpURLConnection method to query the REST service.
     * The data is constructed in the progress method and in the post the data is added to the
     * adapter for the recyclerview.
     */
    private class doNetwork extends AsyncTask<URI, String, String> {

        /*
         * while this could have been in the doInBackground, I reused the
         * method already created the thread class.
         *
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
                    publishProgress(line);  //create the data structure as we go.
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
            try {
                //setup password authentication
                Authenticator.setDefault(new MyAuthenticator());
                //URL url = new URL(params[0].toString()); //but next line is much better! convert directly.
                URL url = params[0].toURL();

                HttpURLConnection con = (HttpURLConnection) url.openConnection();
                page = readStream(con.getInputStream());
                con.disconnect();
            } catch (Exception e) {
                // publishProgress("Failed to retrieve web page ...\n");
                publishProgress(e.getMessage());

            }
            return page;  //return the page downloaded.
        }

        /*
         * build the data structure.
         */
        protected void onProgressUpdate(String... progress) {
            //build the data structure as we go.
            try {
                String parts[] = progress[0].split(",");
                list.add(new myObj(Integer.valueOf(parts[0]), parts[1], parts[2]));
            } catch (Exception e) {
                Log.v("donetwork", "Error line: " + progress[0]);
            }
        }

        /*
         * finished, new set the adapter with the data and turn off the refresh.
         */
        protected void onPostExecute(String result) {
            //data structure is ready.
            mAdapter.setData(list);
            mSwipeRefreshLayout.setRefreshing(false);  //turn of the refresh.
        }
    }

    /*
     *this asynctask is passing parameters via post to the rest service.
     * The post parameters are setup correctly in the dataAsync method that is
     * passed to the task.  It then open the connection, passes the parameters, authenicates
     * and toasts the return value.
     */

    private class doRest extends AsyncTask<myDataAsync, String, Integer> {

        //how to write the parameters via a post method were used from here:
        //http://stackoverflow.com/questions/29536233/deprecated-http-classes-android-lollipop-5-1

        @Override
        protected Integer doInBackground(myDataAsync... params) {
            try {
                //setup password authentication
                Authenticator.setDefault(new MyAuthenticator());
                //setup the url
                URL url = params[0].uri.toURL();
                //make the connection
                HttpURLConnection con = (HttpURLConnection) url.openConnection();
                //setup as post method and write out the parameters.
                con.setRequestMethod("POST");
                con.setDoInput(true);
                con.setDoOutput(true);
                OutputStream os = con.getOutputStream();
                BufferedWriter writer = new BufferedWriter(
                    new OutputStreamWriter(os, "UTF-8"));
                writer.write(params[0].data);
                writer.flush();
                writer.close();
                os.close();

                //get the response code (ie success 200 or something else
                int responseCode = con.getResponseCode();
                String response = "";
                //the return is a single number, so simple to read like this:
                //note the while loop should not be necessary, but just in case.
                if (responseCode == HttpURLConnection.HTTP_OK) {
                    String line;
                    BufferedReader br = new BufferedReader(new InputStreamReader(con.getInputStream()));
                    while ((line = br.readLine()) != null) {
                        response += line;
                    }
                } else
                    response = "0";

                return Integer.valueOf(response);
            } catch (Exception e) {
                // failure of some kind.  uncomment the stacktrace to see what happened if it is
                // permit error.
                //e.printStackTrace();
                return 0;
            }

        }

        protected void onPostExecute(Integer result) {
            Toast.makeText(getApplicationContext(), "Result: " + result, Toast.LENGTH_LONG).show();
            doDataUpdate();  //data has been added/removed, update the recyclerview.
        }
    }
}
