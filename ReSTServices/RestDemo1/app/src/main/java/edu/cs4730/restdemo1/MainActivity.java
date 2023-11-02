package edu.cs4730.restdemo1;

import com.google.android.material.snackbar.Snackbar;

import androidx.appcompat.app.AppCompatActivity;
import androidx.recyclerview.widget.DefaultItemAnimator;
import androidx.recyclerview.widget.LinearLayoutManager;
import androidx.swiperefreshlayout.widget.SwipeRefreshLayout;

import android.os.Bundle;
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

import edu.cs4730.restdemo1.databinding.ActivityMainBinding;

public class MainActivity extends AppCompatActivity {
    String TAG = "MainActivity";
    ActivityMainBinding binding;
    myAdapter mAdapter;
    JSONArray list = null;
    URI uri;

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        binding = ActivityMainBinding.inflate(getLayoutInflater());
        setContentView(binding.getRoot());
        setSupportActionBar(binding.toolbar);

        try {
            uri = new URI("https://jsonplaceholder.typicode.com/posts");
        } catch (URISyntaxException e) {
            Log.wtf(TAG, "error with uri");
            e.printStackTrace();
        }

        binding.fab.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View view) {
                Snackbar.make(view, "Replace with your own action", Snackbar.LENGTH_LONG)
                    .setAction("Action", null).show();
            }
        });

        //setup the RecyclerView

        binding.list.setLayoutManager(new LinearLayoutManager(getApplicationContext()));
        binding.list.setItemAnimator(new DefaultItemAnimator());
        //setup the adapter, which is myAdapter, see the code.  set it initially to null
        mAdapter = new myAdapter(null, getApplicationContext());
        //add the adapter to the recyclerview
        binding.list.setAdapter(mAdapter);

        //SwipeRefreshlayout setup.
        //setup some colors for the refresh circle.
        binding.activityMainSwipeRefreshLayout.setColorSchemeResources(R.color.orange, R.color.green, R.color.blue);
        //now setup the swiperefrestlayout listener where the main work is done.
        binding.activityMainSwipeRefreshLayout.setOnRefreshListener(new SwipeRefreshLayout.OnRefreshListener() {
            @Override
            public void onRefresh() {
                new Thread(new doNetwork(uri)).start();
            }
        });
        binding.activityMainSwipeRefreshLayout.setRefreshing(true);
        //call the refresh code manually here.
        new Thread(new doNetwork(uri)).start();

    }

    /**
     * simple helper method to update the UI.
     */
    public void mkmsg(String str) {
        //so the client uses a handler, while the server code is going to use runUI method.
        //the method name makes no sense here, but keeping the name, so it matches the tpcclient.
        runOnUiThread(new Runnable() {
            @Override
            public void run() {
                try {
                    list = new JSONArray(str);
                    mAdapter.setData(list);
                } catch (JSONException e) {
                    e.printStackTrace();
                    Toast.makeText(getBaseContext(), "Data response is empty", Toast.LENGTH_SHORT).show();
                }
                binding.activityMainSwipeRefreshLayout.setRefreshing(false);  //turn off the refresh.
            }
        });
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
        URI myUri;

        doNetwork(URI uri) {
            myUri = uri;
        }

        public void run() {
            String page = "";
            try {
                URL myUrl = uri.toURL();
                HttpURLConnection con = (HttpURLConnection) myUrl.openConnection();
                page = readStream(con.getInputStream());
                con.disconnect();
            } catch (Exception e) {
                Log.wtf(TAG, e.getMessage());
                return;
            }
            Log.wtf(TAG, "finished, updating result.");
            mkmsg(page);
        }
    }

}
