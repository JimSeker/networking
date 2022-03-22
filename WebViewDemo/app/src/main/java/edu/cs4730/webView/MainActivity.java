package edu.cs4730.webView;

import android.annotation.SuppressLint;
import android.os.Bundle;
import android.view.KeyEvent;
import android.view.Menu;
import android.view.MenuItem;
import android.view.View;
import android.webkit.WebResourceRequest;
import android.webkit.WebView;
import android.webkit.WebViewClient;
import android.widget.Button;

import androidx.appcompat.app.AppCompatActivity;

/**
 * Had to a android:usesCleartextTraffic="true" to get it read none https web sites.  dumb... really stupid android.
 * <p>
 * A simple example that shows how to use the webview widget in an app.
 * Added the safe browsing meta tag to the manifest.  no page to test with so I don't actually know if it works.
 * <p>
 * A note, not all mobile pages allow zooming by default, UW's main page doesn't.  Then again, their web programmers are dumbasses too.  Look at their pages source code, dead obvious.
 */

public class MainActivity extends AppCompatActivity {

    WebView browser;
    Button btnZoomIn, btnZoomOut, btnBack, btnForward;

    @SuppressLint("SetJavaScriptEnabled")  //I want it turned on for demo, stop bitching.
    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_main);
        //setup the WebView object and give it the initial destination.
        browser = findViewById(R.id.webkit);
        browser.getSettings().setJavaScriptEnabled(true);
        browser.getSettings().setBuiltInZoomControls(true);
        browser.loadUrl("https://www.cs.uwyo.edu/~seker/");  //see note about UW main page for why ~seker/

        //setup the callBack, so when the user clicks a link, we intercept it and kept everything in the app.
        browser.setWebViewClient(new CallBack());

        //how buttons from zoom and forward/back.
        btnZoomIn = findViewById(R.id.btnZoomIn);
        btnZoomIn.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View v) {
                browser.zoomIn();
            }
        });
        btnZoomOut = findViewById(R.id.btnZoomOut);
        btnZoomOut.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View v) {
                browser.zoomOut();
            }
        });
        btnBack = findViewById(R.id.btnBack);
        btnBack.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View v) {
                if (browser.canGoBack()) {
                    browser.goBack();
                }
            }
        });
        btnForward = findViewById(R.id.btnFoward);
        btnForward.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View v) {
                if (browser.canGoForward()) {
                    browser.goForward();
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
        if ((keyCode == KeyEvent.KEYCODE_BACK) && browser.canGoBack()) {
            browser.goBack();
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