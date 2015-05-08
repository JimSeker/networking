package edu.cs4730.TCPserv;

import android.os.Bundle;
import android.support.v7.app.AppCompatActivity;

/*  Nothing to see here, got MainFragment
 *
 * Note use adb forward tcp:3012 tcp:3012
 * to setup the emulator to receive.  
 */
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