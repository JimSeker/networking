package edu.cs4730.TCPclient;

import android.os.Bundle;
import android.support.v7.app.AppCompatActivity;

//Nothing to see here.  Goto MainFragment.

public class MainActivity extends AppCompatActivity {
    /** Called when the activity is first created. */

	@Override
    public void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_main);
        if (savedInstanceState == null) {
            getSupportFragmentManager().beginTransaction()
                    .add(R.id.container, new MainFragment()).commit();
        }
    }
}