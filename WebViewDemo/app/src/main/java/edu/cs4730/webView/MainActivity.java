package edu.cs4730.webView;

import android.os.Bundle;
import android.support.v7.app.AppCompatActivity;
import android.view.KeyEvent;
import android.view.Menu;
import android.view.MenuItem;

public class MainActivity extends AppCompatActivity {

	MainFragment myMainFragment = null;
	
	@Override
	protected void onCreate(Bundle savedInstanceState) {
		super.onCreate(savedInstanceState);
		setContentView(R.layout.activity_main);
		if (myMainFragment == null) {
			myMainFragment = new MainFragment();
			getSupportFragmentManager().beginTransaction()
					.add(R.id.container, myMainFragment).commit();
		}
	}

	/*
	 * This is intercepting the back key and then using it to go back in the browser pages
	 * if there is a previous.  See mainFragment for more information.
	 */
    @Override
    public boolean onKeyDown(int keyCode, KeyEvent event) {
        if ((keyCode == KeyEvent.KEYCODE_BACK) && myMainFragment.browser.canGoBack()) {
            myMainFragment.browser.goBack();
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
