package edu.cs4730.httpclientdemo;

import android.os.Bundle;
import android.os.Handler;
import android.os.Message;
import android.support.v7.app.AppCompatActivity;
import android.view.View;
import android.widget.Button;
import android.widget.TextView;

import java.io.BufferedReader;
import java.io.IOException;
import java.io.InputStreamReader;
import java.net.URI;
import org.apache.http.HttpResponse;
import org.apache.http.client.HttpClient;
import org.apache.http.client.methods.HttpGet;
import org.apache.http.impl.client.DefaultHttpClient;

/*
 * note, this example needs to be replaced with the HttpURLConnection instead of HttpClient...
 * http://android-developers.blogspot.com/2011/09/androids-http-clients.html
 */


public class HttpClientDemo extends AppCompatActivity implements Button.OnClickListener{
	TextView output;
	Button  mkconn;

	/** Called when the activity is first created. */
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
		//anonymous thread created here to get the webpage
		// All network must be done on a thread/async now or the OS will force the app.
		new Thread(new doNetwork()).start();

	}

    //handler which can update the screen and in this case show the html and messages.
	private Handler handler = new Handler(new Handler.Callback() {
        @Override
        public boolean handleMessage(Message msg) {
			output.append(msg.getData().getString("msg"));
            return true;
		}

	});

    /*
    * simple method to send messages to the handler.
     */
	public void mkmsg(String str) {
		//handler junk, because thread can't update screen!
		Message msg = new Message();
		Bundle b = new Bundle();
		b.putString("msg", str);
		msg.setData(b);
		handler.sendMessage(msg);
	}


	public void executeHttpGet() throws Exception {
		BufferedReader in = null;
		try {
			HttpClient client = new DefaultHttpClient();
			HttpGet request = new HttpGet();
			request.setURI(new URI("http://www.cs.uwyo.edu/~seker/courses/4730/index.html"));
			mkmsg("Requesting web page.\n");
			HttpResponse response = client.execute(request);
			mkmsg("Web page received, processing it.\n");
			in = new BufferedReader	(new InputStreamReader(response.getEntity().getContent()));
			StringBuilder sb = new StringBuilder("");
			String line = "";
			String NL = System.getProperty("line.separator");
			while ((line = in.readLine()) != null) {
				sb.append(line + NL);
			}
			in.close();
			String page = sb.toString();
			mkmsg("Processed page:");
			mkmsg(page);
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
    //actual thread class that calls executeHpptGet() above.
	class doNetwork  implements Runnable {
		public void run() {
			mkmsg("Attempting to retrieve web page ...\n");
			try {
				executeHttpGet();
				mkmsg("Finished\n");
			} catch (Exception e) {
				mkmsg("Failed to retrieve web page ...\n");
				mkmsg(e.getMessage());
				//e.printStackTrace();
			}
		}
	}



}
