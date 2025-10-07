package edu.cs4730.restdemo3;

import com.google.android.material.snackbar.Snackbar;

import androidx.core.graphics.Insets;
import androidx.core.view.ViewCompat;
import androidx.core.view.WindowInsetsCompat;
import androidx.swiperefreshlayout.widget.SwipeRefreshLayout;
import androidx.appcompat.app.AppCompatActivity;
import androidx.recyclerview.widget.DefaultItemAnimator;
import androidx.recyclerview.widget.LinearLayoutManager;


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

import edu.cs4730.restdemo3.databinding.ActivityMainBinding;

/**
 * Note, https://koz.io/android-m-and-the-war-on-cleartext-traffic/
 * In the AndroidManifest.xml there is < application ... android:usesCleartextTraffic="true" ...
 * The test server doesn't have a legit cert, so... @#$@ it, cleartext it is.
 * For real app, with legit certs on web servers, you should use https and remove the above.
 */
public class MainActivity extends AppCompatActivity {
    String TAG = "MainActivity";
    ActivityMainBinding binding;
    myAdapter mAdapter;

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
        setSupportActionBar(binding.toolbar);

        binding.fab.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View view) {
                Snackbar.make(view, "Replace with your own action", Snackbar.LENGTH_LONG).setAction("Action", null).show();
            }
        });

        //setup the RecyclerView
        binding.list.setLayoutManager(new LinearLayoutManager(getApplicationContext()));
        binding.list.setItemAnimator(new DefaultItemAnimator());
        //setup the adapter, which is myAdapter, see the code.  set it initially to null
        //use the asynctask to set the data later after it is loaded.
        //async task to get size of via query.
        mAdapter = new myAdapter(null,  this);
        //add the adapter to the recyclerview
        binding.list.setAdapter(mAdapter);

        //SwipeRefreshlayout setup.
        //setup some colors for the refresh circle.
        binding.activityMainSwipeRefreshLayout.setColorSchemeResources(R.color.orange, R.color.green, R.color.blue);
        //now setup the swiperefrestlayout listener where the main work is done.
        binding.activityMainSwipeRefreshLayout.setOnRefreshListener(new SwipeRefreshLayout.OnRefreshListener() {
            @Override
            public void onRefresh() {
                //where we call the refresher parts.
                doupdate();
            }
        });
        doupdate();
    }

    void doupdate() {
        binding.activityMainSwipeRefreshLayout.setRefreshing(true);
        try {
            new Thread(new doNetwork(new URL("http://www.cs.uwyo.edu/~seker/rest/querypic.php"))).start();
        } catch (MalformedURLException e) {
            e.printStackTrace();
        }
    }

    /**
     * Shows how to use a thread with a httpURLconnection method to query the REST service.
     * Note this was a asynctask, I just converted to a thread, but keep all the methods.
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
            StringBuilder sb = new StringBuilder();
            String line = "";
            String NL = System.lineSeparator();
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
                    binding.activityMainSwipeRefreshLayout.setRefreshing(false);  //turn of the refresh.
                }
            });
        }
    }

}
