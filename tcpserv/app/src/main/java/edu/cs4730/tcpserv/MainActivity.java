package edu.cs4730.tcpserv;

import androidx.appcompat.app.AppCompatActivity;

import android.os.Bundle;

/**
 * Note, https://koz.io/android-m-and-the-war-on-cleartext-traffic/
 * In the AndroidManifest.xml there is < application ... android:usesCleartextTraffic="true" ...
 * Actually this app didn't need it to work, but that is likely to change.
 * <p>
 * If you are running this in emulators, make sure to run
 * adb forward tcp:3012 tcp:3012  for the server.
 * Do this on the first emulator, then launch the second one and run the client on the second emulator.
 * This assumes port 3012, so if you changed it, then change adb line as well.
 * <p>
 * Note, that while the IP address of the server is listed, if you are running on emulator use the host
 * address in tcpclient ip 10.0.2.2 (this is the address used by android for the host system.
 *
 * If you want to talk the server from program like telnet, or whatever.  localhost: 3012  is the address
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
