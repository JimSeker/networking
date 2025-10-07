package edu.cs4730.webviewdemo_kt

import android.annotation.SuppressLint
import android.os.Bundle
import android.util.Log
import android.view.KeyEvent
import android.view.Menu
import android.view.MenuItem
import android.webkit.WebResourceRequest
import android.webkit.WebView
import android.webkit.WebViewClient
import androidx.appcompat.app.AppCompatActivity
import androidx.core.view.ViewCompat
import androidx.core.view.WindowInsetsCompat
import edu.cs4730.webviewdemo_kt.databinding.ActivityMainBinding

class MainActivity : AppCompatActivity() {
    lateinit var binding: ActivityMainBinding

    @SuppressLint("SetJavaScriptEnabled")
    override fun onCreate(savedInstanceState: Bundle?) {
        super.onCreate(savedInstanceState)
        binding = ActivityMainBinding.inflate(layoutInflater)
        setContentView(binding.getRoot())
        ViewCompat.setOnApplyWindowInsetsListener(binding.main) { v, insets ->
            val systemBars = insets.getInsets(WindowInsetsCompat.Type.systemBars())
            v.setPadding(systemBars.left, systemBars.top, systemBars.right, systemBars.bottom)
            insets
        }
        //setup the WebView object and give it the initial destination.
        binding.webkit.getSettings().javaScriptEnabled = true
        binding.webkit.getSettings().builtInZoomControls = true
        binding.webkit.loadUrl("https://www.eecs.uwyo.edu/~seker/") //see note about UW main page for why ~seker/


        //setup the callBack, so when the user clicks a link, we intercept it and kept everything in the app.
        binding.webkit.setWebViewClient(CallBack())

        //how buttons from zoom and forward/back.
        binding.btnZoomIn.setOnClickListener { binding.webkit.zoomIn() }

        binding.btnZoomOut.setOnClickListener { binding.webkit.zoomOut() }

        binding.btnBack.setOnClickListener {
            if (binding.webkit.canGoBack()) {
                binding.webkit.goBack()
            }
        }

        binding.btnFoward.setOnClickListener {
            if (binding.webkit.canGoForward()) {
                binding.webkit.goForward()
            }
        }
    }

    /**
     * This is override, so i can intercept when a user clicks a link, so it won't leave the app.
     */
    private inner class CallBack : WebViewClient() {
        override fun shouldOverrideUrlLoading(view: WebView, request: WebResourceRequest): Boolean {
            Log.d("over", request.url.toString())
            view.loadUrl(request.url.toString())
            return true
        }
    }

    /**
     * This is intercepting the back key and then using it to go back in the browser pages
     * if there is a previous.  A note in android 16, API 36 if may not intercept the back key anymore.
     */
    override fun onKeyDown(keyCode: Int, event: KeyEvent?): Boolean {
        if ((keyCode == KeyEvent.KEYCODE_BACK) && binding.webkit.canGoBack()) {
            binding.webkit.goBack()
            return true
        }
        return super.onKeyDown(keyCode, event)
    }

    override fun onCreateOptionsMenu(menu: Menu?): Boolean {
        // Inflate the menu; this adds items to the action bar if it is present.
        menuInflater.inflate(R.menu.main, menu)
        return true
    }

    override fun onOptionsItemSelected(item: MenuItem): Boolean {
        //since I'm intercepting the back button, need to provide an exit method.
        val id = item.itemId
        if (id == R.id.exit) {
            finish()
            return true
        } else if (id == R.id.forward) {
            if (binding.webkit.canGoForward()) {
                binding.webkit.goForward()
            }
            return true
        } else if (id == R.id.back) {
            if (binding.webkit.canGoBack()) {
                binding.webkit.goBack()
            }
            return true
        }
        return super.onOptionsItemSelected(item)
    }

}