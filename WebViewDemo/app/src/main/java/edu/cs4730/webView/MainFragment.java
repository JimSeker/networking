package edu.cs4730.webView;

import android.os.Bundle;
import android.support.v4.app.Fragment;
import android.view.LayoutInflater;
import android.view.View;
import android.view.ViewGroup;
import android.view.View.OnClickListener;
import android.webkit.WebView;
import android.webkit.WebViewClient;
import android.widget.Button;

/**
 * A simple example that shows how to use the webview widget in an app.
 * 
 * 
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
        browser=(WebView) myView.findViewById(R.id.webkit);
        browser.getSettings().setJavaScriptEnabled(true);
        browser.getSettings().setBuiltInZoomControls(true);
        browser.loadUrl("http://www.cs.uwyo.edu");
        //browser.loadUrl("");
        //setup the callBack, so when the user clicks a link, we intercept it and kept everything
        //in the app.
        browser.setWebViewClient(new CallBack());
        
        //how buttons from zoom and forward/back.
        btnZoomIn=(Button) myView.findViewById(R.id.btnZoomIn);
        btnZoomIn.setOnClickListener(new OnClickListener(){
			@Override
			public void onClick(View v) {
				browser.zoomIn();
			}
        });
        btnZoomOut=(Button) myView.findViewById(R.id.btnZoomOut);
        btnZoomOut.setOnClickListener(new OnClickListener(){
			@Override
			public void onClick(View v) {
				browser.zoomOut();
			}
        });
        btnBack=(Button) myView.findViewById(R.id.btnBack);
        btnBack.setOnClickListener(new OnClickListener(){
			@Override
			public void onClick(View v) {
				if (browser.canGoBack()) {
					browser.goBack();
				}
			}
        });
        btnForward=(Button) myView.findViewById(R.id.btnFoward);
        btnForward.setOnClickListener(new OnClickListener(){
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
    	public boolean shouldOverrideUrlLoading(WebView view, String url) {
    		browser.loadUrl(url);
    		return true;
    	}
    }
}
