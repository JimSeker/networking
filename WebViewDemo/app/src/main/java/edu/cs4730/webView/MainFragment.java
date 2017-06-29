package edu.cs4730.webView;

import android.os.Build;
import android.os.Bundle;
import android.support.v4.app.Fragment;
import android.view.LayoutInflater;
import android.view.View;
import android.view.ViewGroup;
import android.view.View.OnClickListener;
import android.webkit.WebResourceRequest;
import android.webkit.WebView;
import android.webkit.WebViewClient;
import android.widget.Button;

/**
 * A simple example that shows how to use the webview widget in an app.
 *
 * Added the safe browsing meta tag to the manifest.  no page to test with so I don't actually know if it works.
 *
 * A note, not all mobile pages allow zooming by default, UW's main page doesn't.  Then again, their web programmers are dumbasses too.  Look at their pages source code, dead obvious.
 */
public class MainFragment extends Fragment {

    WebView browser;
    Button btnZoomIn, btnZoomOut, btnBack, btnForward;

    public MainFragment() {
        // Required empty public constructor
    }

    @Override
    public View onCreateView(LayoutInflater inflater, ViewGroup container,
                             Bundle savedInstanceState) {
        // Inflate the layout for this fragment
        View myView = inflater.inflate(R.layout.fragment_main, container, false);

        //setup the WebView object and give it the initial destination.
        browser = (WebView) myView.findViewById(R.id.webkit);
        browser.getSettings().setJavaScriptEnabled(true);
        browser.getSettings().setBuiltInZoomControls(true);
        browser.loadUrl("http://www.cs.uwyo.edu/~seker/");  //see note about UW main page for why ~seker/

        //setup the callBack, so when the user clicks a link, we intercept it and kept everything
        //in the app.
        browser.setWebViewClient(new CallBack());

        //how buttons from zoom and forward/back.
        btnZoomIn = (Button) myView.findViewById(R.id.btnZoomIn);
        btnZoomIn.setOnClickListener(new OnClickListener() {
            @Override
            public void onClick(View v) {
                browser.zoomIn();
            }
        });
        btnZoomOut = (Button) myView.findViewById(R.id.btnZoomOut);
        btnZoomOut.setOnClickListener(new OnClickListener() {
            @Override
            public void onClick(View v) {
                browser.zoomOut();
            }
        });
        btnBack = (Button) myView.findViewById(R.id.btnBack);
        btnBack.setOnClickListener(new OnClickListener() {
            @Override
            public void onClick(View v) {
                if (browser.canGoBack()) {
                    browser.goBack();
                }
            }
        });
        btnForward = (Button) myView.findViewById(R.id.btnFoward);
        btnForward.setOnClickListener(new OnClickListener() {
            @Override
            public void onClick(View v) {
                if (browser.canGoForward()) {
                    browser.goForward();
                }
            }
        });

        return myView;
    }

    /*
     * This is override, so i can intercept when a user clicks a link, so it won't leave the app.
     */
    private class CallBack extends WebViewClient {

        //API 24+, so the lollipop check is just for studio to shutup about it.
        public boolean shouldOverrideUrlLoading(WebView view, WebResourceRequest request) {
            if (Build.VERSION.SDK_INT >= Build.VERSION_CODES.LOLLIPOP) {
                view.loadUrl(request.getUrl().toString());
            }
            return true;
        }

        //deprecated in API 24
        public boolean shouldOverrideUrlLoading(WebView view, String url) {

            browser.loadUrl(url);
            return true;
        }
    }
}
