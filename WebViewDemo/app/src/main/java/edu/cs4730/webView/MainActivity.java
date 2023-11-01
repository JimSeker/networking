package edu.cs4730.webView;

import android.annotation.SuppressLint;
import android.os.Bundle;
import android.util.Log;
import android.view.KeyEvent;
import android.view.Menu;
import android.view.MenuItem;
import android.view.View;
import android.webkit.WebResourceRequest;
import android.webkit.WebView;
import android.webkit.WebViewClient;

import androidx.appcompat.app.AppCompatActivity;

import edu.cs4730.webView.databinding.ActivityMainBinding;

/**
 * Had to a android:usesCleartextTraffic="true" to get it read none https web sites.  dumb... really stupid android.
 * <p>
 * A simple example that shows how to use the webview widget in an app.
 * Added the safe browsing meta tag to the manifest.  no page to test with so I don't actually know if it works.
 * <p>
 * A note, not all mobile pages allow zooming by default, UW's main page doesn't.  Then again, their web programmers are dumbasses too.  Look at their pages source code, dead obvious.
 */

public class MainActivity extends AppCompatActivity {

    ActivityMainBinding binding;

    @SuppressLint("SetJavaScriptEnabled")  //I want it turned on for demo, stop bitching.
    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        binding = ActivityMainBinding.inflate(getLayoutInflater());
        setContentView(binding.getRoot());
        //setup the WebView object and give it the initial destination.

        binding.webkit.getSettings().setJavaScriptEnabled(true);
        binding.webkit.getSettings().setBuiltInZoomControls(true);
        binding.webkit.loadUrl("https://www.eecs.uwyo.edu/~seker/");  //see note about UW main page for why ~seker/

        //setup the callBack, so when the user clicks a link, we intercept it and kept everything in the app.
        binding.webkit.setWebViewClient(new CallBack());

        //how buttons from zoom and forward/back.

        binding.btnZoomIn.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View v) {
                binding.webkit.zoomIn();
            }
        });

        binding.btnZoomOut.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View v) {
                binding.webkit.zoomOut();
            }
        });

        binding.btnBack.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View v) {
                if (binding.webkit.canGoBack()) {
                    binding.webkit.goBack();
                }
            }
        });

        binding.btnFoward.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View v) {
                if (binding.webkit.canGoForward()) {
                    binding.webkit.goForward();
                }
            }
        });

    }

    /**
     * This is override, so i can intercept when a user clicks a link, so it won't leave the app.
     */
    private class CallBack extends WebViewClient {

        //API 24+, so the N check is just for studio to shut up about it.
        public boolean shouldOverrideUrlLoading(WebView view, WebResourceRequest request) {
            Log.d("over", request.getUrl().toString());
            view.loadUrl(request.getUrl().toString());
            return true;
        }

    }

    /**
     * This is intercepting the back key and then using it to go back in the browser pages
     * if there is a previous.
     */

    @Override
    public boolean onKeyDown(int keyCode, KeyEvent event) {
        if ((keyCode == KeyEvent.KEYCODE_BACK) && binding.webkit.canGoBack()) {
            binding.webkit.goBack();
            return true;
        }
        return super.onKeyDown(keyCode, event);
    }

    @Override
    public boolean onCreateOptionsMenu(Menu menu) {
        // Inflate the menu; this adds items to the action bar if it is present.
        getMenuInflater().inflate(R.menu.main, menu);
        return true;
    }

    @Override
    public boolean onOptionsItemSelected(MenuItem item) {
        //since I'm intercepting the back button, need to provide an exit method.
        int id = item.getItemId();
        if (id == R.id.exit) {
            finish();
            return true;
        }
        return super.onOptionsItemSelected(item);
    }
}