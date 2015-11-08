package edu.cs4730.restdemo3;

import android.os.AsyncTask;
import android.os.Bundle;
import android.support.design.widget.FloatingActionButton;
import android.support.design.widget.Snackbar;
import android.support.v4.widget.SwipeRefreshLayout;
import android.support.v7.app.AppCompatActivity;
import android.support.v7.widget.DefaultItemAnimator;
import android.support.v7.widget.LinearLayoutManager;
import android.support.v7.widget.RecyclerView;
import android.support.v7.widget.Toolbar;
import android.util.Log;
import android.view.View;
import android.view.Menu;
import android.view.MenuItem;

import java.io.BufferedReader;
import java.io.IOException;
import java.io.InputStream;
import java.io.InputStreamReader;
import java.net.Authenticator;
import java.net.HttpURLConnection;
import java.net.MalformedURLException;
import java.net.URL;
import java.util.ArrayList;

public class MainActivity extends AppCompatActivity {
    String TAG = "MainActivity";
    RecyclerView mRecyclerView;
    myAdapter mAdapter;
    SwipeRefreshLayout mSwipeRefreshLayout;


    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_main);
        Toolbar toolbar = (Toolbar) findViewById(R.id.toolbar);
        setSupportActionBar(toolbar);

        FloatingActionButton fab = (FloatingActionButton) findViewById(R.id.fab);
        fab.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View view) {
                Snackbar.make(view, "Replace with your own action", Snackbar.LENGTH_LONG)
                        .setAction("Action", null).show();
            }
        });


        //setup the RecyclerView
        mRecyclerView = (RecyclerView) findViewById(R.id.list);
        mRecyclerView.setLayoutManager(new LinearLayoutManager(getApplicationContext()));
        mRecyclerView.setItemAnimator(new DefaultItemAnimator());
        //setup the adapter, which is myAdapter, see the code.  set it initially to null
        //use the asynctask to set the data later after it is loaded.
        //async task to get size of via query.
        mAdapter = new myAdapter(null, R.layout.rowlayout, getApplicationContext(), getSupportFragmentManager());
        //add the adapter to the recyclerview
        mRecyclerView.setAdapter(mAdapter);

        //SwipeRefreshlayout setup.
        mSwipeRefreshLayout = (SwipeRefreshLayout) findViewById(R.id.activity_main_swipe_refresh_layout);
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
            new doNetwork().execute(new URL("http://www.cs.uwyo.edu/~seker/rest/querypic.php"));
        } catch (MalformedURLException e) {
            e.printStackTrace();
        }

    }
    /*
 * Shows how to use an AsyncTask with a httpURLconnection method to query the REST service.
 */
    class doNetwork extends AsyncTask<URL, String, String> {
        ArrayList<myObj> list = null;
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
        protected String doInBackground(URL... params) {
            String page = "";
            list = new ArrayList<myObj>();
            try {
                //setup password authentication
                Authenticator.setDefault(new myAdapter.MyAuthenticator());
                //URL url = new URL(params[0].toString()); //but next line is much better! convert directly.
                URL url = params[0];

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
         * This takes the place of handlers and my makemsg method, since we can directly access the screen.
         */
        protected void onProgressUpdate(String... progress) {
            //build the data structure as we go.
            // output.append(progress[0]);
            //Log.v("progress", progress[0]);
            try {
                String parts[] = progress[0].split(",");
                //Log.v("done", parts[0] + " " + parts[1] + " " + parts[2]);
                list.add(new myObj(Integer.valueOf(parts[0]), parts[1], parts[2]));

            } catch (Exception e) {
                Log.v("donetwork", "Error line: " + progress[0]);
                e.printStackTrace();
            }
        }

        /*
         * So the file has been downloaded and in this simple example it is displayed
         * to the screen.
         */
        protected void onPostExecute(String result) {
            //data structure is ready.
            mAdapter.setData(list);
            mSwipeRefreshLayout.setRefreshing(false);  //turn of the refresh.

        }
    }

}
