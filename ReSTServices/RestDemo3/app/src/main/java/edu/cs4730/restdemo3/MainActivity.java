package edu.cs4730.restdemo3;

import com.google.android.material.floatingactionbutton.FloatingActionButton;
import com.google.android.material.snackbar.Snackbar;

import androidx.swiperefreshlayout.widget.SwipeRefreshLayout;
import androidx.appcompat.app.AppCompatActivity;
import androidx.recyclerview.widget.DefaultItemAnimator;
import androidx.recyclerview.widget.LinearLayoutManager;
import androidx.recyclerview.widget.RecyclerView;
import androidx.appcompat.widget.Toolbar;

import android.util.Log;
import android.view.View;
import android.os.Bundle;

import java.io.BufferedReader;
import java.io.IOException;
import java.io.InputStream;
import java.io.InputStreamReader;
import java.net.Authenticator;
import java.net.HttpURLConnection;
import java.net.MalformedURLException;
import java.net.URL;
import java.util.ArrayList;

/**
 * Note, https://koz.io/android-m-and-the-war-on-cleartext-traffic/
 * In the AndroidManifest.xml there is < application ... android:usesCleartextTraffic="true" ...
 * The test server doesn't have a legit cert, so... @#$@ it, cleartext it is.
 * For real app, with legit certs on web servers, you should use https and remove the above.
 */
public class MainActivity extends AppCompatActivity {
    String TAG = "MainActivity";
    RecyclerView mRecyclerView;
    myAdapter mAdapter;
    SwipeRefreshLayout mSwipeRefreshLayout;

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_main);
        Toolbar toolbar = findViewById(R.id.toolbar);
        setSupportActionBar(toolbar);

        FloatingActionButton fab = findViewById(R.id.fab);
        fab.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View view) {
                Snackbar.make(view, "Replace with your own action", Snackbar.LENGTH_LONG)
                    .setAction("Action", null).show();
            }
        });


        //setup the RecyclerView
        mRecyclerView = findViewById(R.id.list);
        mRecyclerView.setLayoutManager(new LinearLayoutManager(getApplicationContext()));
        mRecyclerView.setItemAnimator(new DefaultItemAnimator());
        //setup the adapter, which is myAdapter, see the code.  set it initially to null
        //use the asynctask to set the data later after it is loaded.
        //async task to get size of via query.
        mAdapter = new myAdapter(null, R.layout.rowlayout, this);
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
                doupdate();
            }
        });
        doupdate();
    }

    void doupdate() {
        mSwipeRefreshLayout.setRefreshing(true);
        try {
            new Thread(new doNetwork(new URL("http://www.cs.uwyo.edu/~seker/rest/querypic.php"))).start();
        } catch (MalformedURLException e) {
            e.printStackTrace();
        }
    }

    /**
     * Shows how to use a thread with a httpURLconnection method to query the REST service.
     * Note this was a asynctask, I just convereted to a thread, but keep all the methods.
     */
    private class doNetwork implements Runnable {
        ArrayList<myObj> list = null;
        URL myUrl;

        doNetwork(URL url) {
            myUrl = url;
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
                    onProgressUpdate(line);  //create the data structure as we go.
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


        public void run() {
            String page = "";
            list = new ArrayList<myObj>();
            try {
                //setup password authentication
                Authenticator.setDefault(new myAdapter.MyAuthenticator());
                HttpURLConnection con = (HttpURLConnection) myUrl.openConnection();
                page = readStream(con.getInputStream());
                con.disconnect();
            } catch (Exception e) {
                // publishProgress("Failed to retrieve web page ...\n");
                e.printStackTrace();

            }
            onPostExecute(page);  //return the page downloaded.
        }

        protected void onProgressUpdate(String progress) {
            //build the data structure as we go.

            try {
                String[] parts = progress.split(",");
                if (parts.length >= 2) {
                    Log.v("done", parts[0] + " " + parts[1] + " " + parts[2]);
                    list.add(new myObj(Integer.parseInt(parts[0]), parts[1], parts[2]));
                } else {
                    Log.wtf(TAG, "split failed, " + progress);
                }
            } catch (Exception e) {
                Log.v("donetwork", "Error line: " + progress);
                e.printStackTrace();
            }
        }

        /**
         * So the file has been downloaded and in this simple example it is displayed to the screen.
         */
        protected void onPostExecute(String result) {
            //data structure is ready.
            runOnUiThread(new Runnable() {
                @Override
                public void run() {
                    mAdapter.setData(list);
                    mSwipeRefreshLayout.setRefreshing(false);  //turn of the refresh.
                }
            });
        }
    }

}
