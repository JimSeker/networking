package edu.cs4730.restdemo1;

import android.os.AsyncTask;
import android.os.Bundle;

import com.google.android.material.floatingactionbutton.FloatingActionButton;
import com.google.android.material.snackbar.Snackbar;

import androidx.appcompat.app.AppCompatActivity;
import androidx.recyclerview.widget.DefaultItemAnimator;
import androidx.recyclerview.widget.LinearLayoutManager;
import androidx.recyclerview.widget.RecyclerView;
import androidx.appcompat.widget.Toolbar;
import androidx.swiperefreshlayout.widget.SwipeRefreshLayout;

import android.util.Log;
import android.view.View;
import android.widget.Toast;

import org.json.JSONArray;
import org.json.JSONException;

import java.io.BufferedReader;
import java.io.IOException;
import java.io.InputStream;
import java.io.InputStreamReader;
import java.net.HttpURLConnection;
import java.net.URI;
import java.net.URISyntaxException;
import java.net.URL;

public class MainActivity extends AppCompatActivity {
    String TAG = "MainActivity";
    RecyclerView mRecyclerView;
    myAdapter mAdapter;
    SwipeRefreshLayout mSwipeRefreshLayout;
    JSONArray list = null;
    URI uri;

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_main);
        Toolbar toolbar = findViewById(R.id.toolbar);
        setSupportActionBar(toolbar);
        try {
            uri = new URI("https://jsonplaceholder.typicode.com/posts");
        } catch (URISyntaxException e) {
            Log.wtf(TAG, "error with uri");
            e.printStackTrace();
        }
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
        mAdapter = new myAdapter(null, R.layout.rowlayout, getApplicationContext());
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
                //where we call the refresher parts.  normally some kind of networking async task or web service.
                // For demo purpose, the code in the refreshslower method so it will take a couple of seconds
                //otherise, the task or service would just be called here.
                new doNetwork().execute(uri);
                //refreshslower();  //this will be slower, for the demo.
            }
        });
        mSwipeRefreshLayout.setRefreshing(true);
        //call the refresh code manually here.
        new doNetwork().execute(uri);

    }

    /*
     * Shows how to use an AsyncTask with a HttpClient method.
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
            //Log.wtf(TAG, "readstream started.");
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
            try {
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
         * This takes the place of handlers and my makemsg method, since we can directly access the screen.
         */
        protected void onProgressUpdate(String... progress) {
            Log.wtf(TAG, progress[0]);
        }

        /*
         * So the file has been downloaded and in this simple example it is displayed
         * to the screen.
         */
        protected void onPostExecute(String result) {
            Log.wtf(TAG, "finished, updating result.");
            // Log.wtf(TAG, "data: " + result);
            try {
                list = new JSONArray(result);
                mAdapter.setData(list);
            } catch (JSONException e) {
                e.printStackTrace();
                Toast.makeText(getBaseContext(), "Data response is empty", Toast.LENGTH_SHORT).show();
            }
            mSwipeRefreshLayout.setRefreshing(false);  //turn of the refresh.
        }


    }
}
