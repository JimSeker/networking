package edu.cs4730.restdemo2v2;

import android.annotation.SuppressLint;
import android.app.Application;
import android.util.Log;

import androidx.annotation.NonNull;
import androidx.lifecycle.AndroidViewModel;
import androidx.lifecycle.LiveData;
import androidx.lifecycle.MutableLiveData;


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
import java.net.URL;
import java.net.URLEncoder;
import java.nio.charset.StandardCharsets;
import java.util.ArrayList;
import java.util.HashMap;
import java.util.Map;
import java.util.concurrent.ExecutorService;
import java.util.concurrent.Executors;


/**
 * This viewmodel is now do all the heavy lifting.  it deals with all the network code and
 * returns uses the oberser to update the query.
 */

public class DataViewModel extends AndroidViewModel {

    static API api;

    MutableLiveData<ArrayList<myObj>> mlist;
    // Background thread pool for I/O operations
    private final ExecutorService executorService = Executors.newSingleThreadExecutor();

    @SuppressLint("ThreadConstraint")
    public DataViewModel(@NonNull Application application) {
        super(application);
        api = new API();
        mlist = new MutableLiveData<>();

        query();
    }

    public LiveData<ArrayList<myObj>> getData() {
        return mlist;
    }

    /*
     * This helper method will start the query and return back an updated query list, ending
     * with the observers being notified.  via the doNetwork thread.
     */
    public void query() {
        executorService.execute(new doNetwork(api.Query));
    }

    /*
    * this is a helper method to start an insert rest call. It triggers a call to getQuery.
     */
    public void insert(String title, String body) {
        executorService.execute(new doRest(api.Insert, new MyData(0, title, body)));
    }

    /*
    * this is a helper method to start an update rest call.  It triggers a call to getQuery.
     */
    public void update(int id, String title, String body) {
        executorService.execute(new doRest(api.Update, new MyData(id, title, body)));
    }

    /*
      * this is a helper method to start a delete rest call.  It triggers a call to getQuery
     */
    public void delete(int id) {
        executorService.execute(new doRest(api.Delete, new MyData(id, "", "")));
    }

    /**
     * uses an thread with a httpURLConnection method to query the REST service.
     * The data is constructed in the progress method and in the post the data is added to the
     * adapter for the recyclerview.
     * Note this was an aysnctask, which I just made back into a thread, but kept method names.
     */
    private class doNetwork implements Runnable {

        URI myUri;

        doNetwork(URI uri) {
            myUri = uri;
        }

        //Simple class that takes an InputStream and return the data
        //as a string, with line separators (ie end of line markers)
        private ArrayList<myObj> readStream(InputStream in) {
            BufferedReader reader = null;
            StringBuilder sb = new StringBuilder();
            String line = "";
            ArrayList<myObj> list = new ArrayList<>();
            try {
                reader = new BufferedReader(new InputStreamReader(in));
                while ((line = reader.readLine()) != null) {
                    String[] parts = line.split(",");
                    //Log.v("doNetwork", "Reading line: " + line);
                    list.add(new myObj(Integer.parseInt(parts[0]), parts[1], parts[2]));
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
            return list;
        }

        public void run() {
            String page = "";
            try {
                //setup password authentication
                Authenticator.setDefault(new MyAuthenticator());
                //URL url = new URL(params[0].toString()); //but next line is much better! convert directly.
                URL url = myUri.toURL();

                HttpURLConnection con = (HttpURLConnection) url.openConnection();
                mlist.postValue(readStream(con.getInputStream()));
                con.disconnect();
            } catch (Exception e) {
                // publishProgress("Failed to retrieve web page ...\n");
                e.printStackTrace();
                mlist.postValue(null);
            }
        }
    }

    /**
     * this thread to passing parameters via post to the rest service.
     * The post parameters are set up correctly in the dataAsync method that is
     * passed to the task.  It then opens the connection, passes the parameters, authenticates
     * and toasts the return value.
     */
    private class doRest implements Runnable {

        URI myUri;
        MyData myData;

        doRest(URI uri, MyData data) {
            myUri = uri;
            myData = data;
        }
        //how to write the parameters via a post method were used from here:
        //http://stackoverflow.com/questions/29536233/deprecated-http-classes-android-lollipop-5-1

        public void run() {
            try {
                //setup password authentication
                Authenticator.setDefault(new MyAuthenticator());
                //set up the url
                URL url = myUri.toURL();
                //make the connection
                HttpURLConnection con = (HttpURLConnection) url.openConnection();
                //setup as post method and write out the parameters.
                con.setRequestMethod("POST");
                con.setDoInput(true);
                con.setDoOutput(true);
                OutputStream os = con.getOutputStream();
                BufferedWriter writer = new BufferedWriter(
                    new OutputStreamWriter(os, StandardCharsets.UTF_8));
                writer.write(myData.data);
                writer.flush();
                writer.close();
                os.close();

                //get the response code (ie success 200 or something else
                int responseCode = con.getResponseCode();
                StringBuilder response = new StringBuilder();
                //the return is a single number, so simple to read like this:
                //note the while loop should not be necessary, but just in case.
                if (responseCode == HttpURLConnection.HTTP_OK) {
                    String line;
                    BufferedReader br = new BufferedReader(new InputStreamReader(con.getInputStream()));
                    while ((line = br.readLine()) != null) {
                        response.append(line);
                    }
                } else
                    response = new StringBuilder("0");

                StringBuilder finalResponse = response;
                //Toast.makeText(getApplication().getBaseContext(), "Result: " + finalResponse.toString(), Toast.LENGTH_LONG).show();
                Log.i("doRest", "Result: " + finalResponse.toString());
                // success now update
                query();
            } catch (Exception e) {
                // failure of some kind.  uncomment the stacktrace to see what happened if it is permit error.
                e.printStackTrace();
            }

        }
    }

    //authentication code used from:
    //http://developer.android.com/reference/java/net/HttpURLConnection.html
    //and http://stackoverflow.com/questions/4883100/how-to-handle-http-authentication-using-httpurlconnection

    private static class MyAuthenticator extends Authenticator {
        public PasswordAuthentication getPasswordAuthentication() {
            // I haven't checked getRequestingScheme() here, since for NTLM
            // and Negotiate, the usrname and password are all the same.
            System.err.println("Feeding username and password for " + getRequestingScheme());
            return (new PasswordAuthentication(API.kuser, API.kpass.toCharArray()));
        }
    }

    @Override
    protected void onCleared() {
        //super.onCleared();
        executorService.shutdown(); // Clean up executor when ViewModel is destroyed
    }


    //helper class to send all the data.  mostly because setting up all the post data is just a pain.
    class MyData {
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
        MyData(int id, String title, String body) {
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
}
