package edu.cs4730.tcpserv;

import androidx.appcompat.app.AppCompatActivity;

import android.os.Bundle;

/**
 * Note, https://koz.io/android-m-and-the-war-on-cleartext-traffic/
 * In the AndroidManifest.xml there is < application ... android:usesCleartextTraffic="true" ...
 * Actually this app didn't need it to work, but that is likely to change.
 *
 * If you are running this in emulators, make sure to run
 *  adb forward tcp:3012 tcp:3012  for the server.
 *  This assumes port 3012, so if you changed it, then change adb line as well.
 *
 */

public class MainActivity extends AppCompatActivity {

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_main);
        if (savedInstanceState == null) {
            getSupportFragmentManager().beginTransaction()
                .add(R.id.container, new MainFragment()).commit();
        }
    }
}
