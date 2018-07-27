package edu.cs4730.downloaddemo;

import java.io.FileNotFoundException;

import android.Manifest;
import android.app.NotificationChannel;
import android.app.NotificationManager;
import android.content.pm.PackageManager;
import android.graphics.Color;
import android.net.Uri;
import android.os.Bundle;
import android.os.Environment;
import android.os.ParcelFileDescriptor;
import android.app.DownloadManager;
import android.content.BroadcastReceiver;
import android.content.Context;
import android.content.Intent;
import android.content.IntentFilter;
import android.database.Cursor;
import android.support.annotation.NonNull;
import android.support.v4.app.ActivityCompat;
import android.support.v7.app.AppCompatActivity;
import android.util.Log;
import android.view.View;
import android.widget.Button;
import android.widget.Toast;

/*
 * http://developer.android.com/reference/android/app/DownloadManager.Request.html#setNotificationVisibility%28int%29
 * http://developer.android.com/reference/android/app/DownloadManager.html
 * http://sunil-android.blogspot.com/2013/01/pass-data-from-service-to-activity.html
 * http://stackoverflow.com/questions/7239996/android-downloadmanager-api-opening-file-after-download
 * http://blog.vogella.com/2011/06/14/android-downloadmanager-example/
 *
 *  newer tutorials that align with api 24+
 * https://www.androidtutorialpoint.com/networking/android-download-manager-tutorial-download-file-using-download-manager-internet/
 * http://www.gadgetsaint.com/android/download-manager/
 */


public class MainActivity extends AppCompatActivity {
    //for the channel id notification.
    public static String id = "test_channel_01";

    //big file, takes about 30 seconds to download
    //String Download_path = "http://www.nasa.gov/images/content/206402main_jsc2007e113280_hires.jpg";
    //String Download_filename = "nasa.jpg";
    String Download_path = "https://www.androidtutorialpoint.com/wp-content/uploads/2016/09/AndroidDownloadManager.mp3";
    String Download_filename = "AndroidDownloadManager.mp3";
    //smaller test file.
    //String Download_path = "http://www.cs.uwyo.edu/~seker/courses/2150/30mbHD.jpg";
    long download_id = -1;

    //SharedPreferences preferenceManager;
    DownloadManager downloadManager;

    String TAG = "MainActivity";
    public static final int REQUEST_PERM_ACCESS_noti = 1;
    public static final int REQUEST_PERM_ACCESS_nonoti = 2;

    /**
     * Called when the activity is first created.
     */
    @Override
    public void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_main);

        downloadManager = (DownloadManager) getSystemService(DOWNLOAD_SERVICE);

        Button btnDownload = (Button) findViewById(R.id.download);
        btnDownload.setOnClickListener(new Button.OnClickListener() {

            @Override
            public void onClick(View arg0) {
                if (ActivityCompat.checkSelfPermission(getApplicationContext(), Manifest.permission.WRITE_EXTERNAL_STORAGE) != PackageManager.PERMISSION_GRANTED) {
                    //I'm on not explaining why, just asking for permission.
                    Log.v(TAG, "asking for permissions");
                    ActivityCompat.requestPermissions(MainActivity.this, new String[]{Manifest.permission.WRITE_EXTERNAL_STORAGE},
                        MainActivity.REQUEST_PERM_ACCESS_noti);

                } else {
                    Log.v(TAG, "already have permissions");
                    downloadfile();
                }
            }
        });
        Button btnDownload2 = (Button) findViewById(R.id.download2);
        btnDownload2.setOnClickListener(new Button.OnClickListener() {

            @Override
            public void onClick(View arg0) {
                downloadfilenonoti();
            }
        });
    }

    void downloadfile() {
        //setup download and how it will look on the notification bar.
        Uri Download_Uri = Uri.parse(Download_path);
        DownloadManager.Request request = new DownloadManager.Request(Download_Uri)
            .setAllowedNetworkTypes(DownloadManager.Request.NETWORK_WIFI | DownloadManager.Request.NETWORK_MOBILE)
            .setAllowedOverRoaming(false)
            .setTitle(Download_filename)
            .setDescription("Cool picture of the lander.")
            .setVisibleInDownloadsUi(true)  //show up in system download manager list.
            .setNotificationVisibility(DownloadManager.Request.VISIBILITY_VISIBLE_NOTIFY_COMPLETED)
            //notify only when completed.
            //.setNotificationVisibility(DownloadManager.Request.VISIBILITY_VISIBLE_NOTIFY_ONLY_COMPLETION)
            .setDestinationInExternalPublicDir(Environment.DIRECTORY_DOWNLOADS, Download_filename);
            //for inside the app space, use
            //.setDestinationInExternalFilesDir(MainActivity.this, Environment.DIRECTORY_DOWNLOADS,Download_filename);
        download_id = downloadManager.enqueue(request);
    }

    void downloadfilenonoti() {

        if (ActivityCompat.checkSelfPermission(getApplicationContext(), Manifest.permission.WRITE_EXTERNAL_STORAGE) != PackageManager.PERMISSION_GRANTED) {
            //I'm on not explaining why, just asking for permission.
            Log.v(TAG, "asking for permissions");
            ActivityCompat.requestPermissions(this, new String[]{Manifest.permission.WRITE_EXTERNAL_STORAGE},
                MainActivity.REQUEST_PERM_ACCESS_nonoti);
            return;
        }
        Log.v(TAG, "already have permissions");

        //This should down the file without creating a notification.
        Uri Download_Uri = Uri.parse(Download_path);
        DownloadManager.Request request = new DownloadManager.Request(Download_Uri)
            //.setAllowedNetworkTypes(DownloadManager.Request.NETWORK_WIFI | DownloadManager.Request.NETWORK_MOBILE);
            //.setAllowedOverRoaming(true)
            .setNotificationVisibility(DownloadManager.Request.VISIBILITY_HIDDEN)  //api 11 and above!
            .setDestinationInExternalPublicDir(Environment.DIRECTORY_DOWNLOADS, Download_filename);
        download_id = downloadManager.enqueue(request);
    }


    @Override
    protected void onResume() {

        super.onResume();

        IntentFilter intentFilter = new IntentFilter(DownloadManager.ACTION_DOWNLOAD_COMPLETE);
        registerReceiver(downloadReceiver, intentFilter);
    }

    @Override
    protected void onPause() {
        super.onPause();

        unregisterReceiver(downloadReceiver);
    }

    private BroadcastReceiver downloadReceiver = new BroadcastReceiver() {
        long intentdownloadId;

        @Override
        public void onReceive(Context context, Intent intent) {
            String action = intent.getAction();
            if (DownloadManager.ACTION_DOWNLOAD_COMPLETE.equals(action)) {
                Bundle extras = intent.getExtras();
                intentdownloadId = extras.getLong(DownloadManager.EXTRA_DOWNLOAD_ID, -1);
                Toast.makeText(MainActivity.this, "should match: id is " + download_id + " int_id is " + intentdownloadId,
                    Toast.LENGTH_LONG).show();
            }

            DownloadManager.Query query = new DownloadManager.Query();
            query.setFilterById(intentdownloadId);
            Cursor cursor = downloadManager.query(query);

            if (cursor.moveToFirst()) {
                int columnIndex = cursor.getColumnIndex(DownloadManager.COLUMN_STATUS);
                int status = cursor.getInt(columnIndex);
                int columnReason = cursor.getColumnIndex(DownloadManager.COLUMN_REASON);
                int reason = cursor.getInt(columnReason);
                int columnURI = cursor.getColumnIndex(DownloadManager.COLUMN_LOCAL_URI);
                String Fname;
                if (columnURI >= 0) {
                    Fname = cursor.getString(columnURI);
                } else {
                    Fname = "unknown";
                }
                if (status == DownloadManager.STATUS_SUCCESSFUL) {
                    try {
                        ParcelFileDescriptor file = downloadManager.openDownloadedFile(intentdownloadId);
                        Toast.makeText(MainActivity.this,
                            "File Downloaded: " + Fname + " and ready to process",
                            Toast.LENGTH_LONG).show();
                    } catch (FileNotFoundException e) {

                        e.printStackTrace();
                        Toast.makeText(MainActivity.this,
                            e.toString(),
                            Toast.LENGTH_LONG).show();
                    }

                } else if (status == DownloadManager.STATUS_FAILED) {
                    Toast.makeText(MainActivity.this,
                        "FAILED!\n" + "reason of " + reason,
                        Toast.LENGTH_LONG).show();
                } else if (status == DownloadManager.STATUS_PAUSED) {
                    Toast.makeText(MainActivity.this,
                        "PAUSED!\n" + "reason of " + reason,
                        Toast.LENGTH_LONG).show();
                } else if (status == DownloadManager.STATUS_PENDING) {
                    Toast.makeText(MainActivity.this,
                        "PENDING!",
                        Toast.LENGTH_LONG).show();
                } else if (status == DownloadManager.STATUS_RUNNING) {
                    Toast.makeText(MainActivity.this,
                        "RUNNING!",
                        Toast.LENGTH_LONG).show();
                }
            }
        }

    };


    //handle the response.
    @Override
    public void onRequestPermissionsResult(int requestCode,
                                           @NonNull String permissions[], @NonNull int[] grantResults) {
        switch (requestCode) {
            case REQUEST_PERM_ACCESS_noti: {
                // If request is cancelled, the result arrays are empty.
                if (grantResults.length > 0
                    && grantResults[0] == PackageManager.PERMISSION_GRANTED) {

                    // write permission was granted, yay!  Now kick off the download manager.
                    downloadfile();

                } else {

                    // permission denied, boo! Disable the
                    // functionality that depends on this permission.
                    Log.v(TAG, "write permissions not granted.");
                }
                return;
            }
            case REQUEST_PERM_ACCESS_nonoti: {
                // If request is cancelled, the result arrays are empty.
                if (grantResults.length > 0
                    && grantResults[0] == PackageManager.PERMISSION_GRANTED) {

                    // write permission was granted, yay!  Now kick off the download manager.
                    downloadfilenonoti();

                } else {

                    // permission denied, boo! Disable the
                    // functionality that depends on this permission.
                    Log.v(TAG, "write permissions not granted.");
                }
            }
            // other 'case' lines to check for other
            // permissions this app might request
        }
    }

    /*

     * for API 26+ create notification channels
  */
    private void createchannel() {
        if (android.os.Build.VERSION.SDK_INT >= android.os.Build.VERSION_CODES.O) {
            NotificationManager nm = (NotificationManager) getSystemService(Context.NOTIFICATION_SERVICE);
            NotificationChannel mChannel = new NotificationChannel(id,
                getString(R.string.channel_name),  //name of the channel
                NotificationManager.IMPORTANCE_DEFAULT);   //importance level
            //important level: default is is high on the phone.  high is urgent on the phone.  low is medium, so none is low?
            // Configure the notification channel.
            mChannel.setDescription(getString(R.string.channel_description));
            mChannel.enableLights(true);
            //Sets the notification light color for notifications posted to this channel, if the device supports this feature.
            mChannel.setLightColor(Color.RED);
            mChannel.enableVibration(true);
            mChannel.setShowBadge(true);
            mChannel.setVibrationPattern(new long[]{100, 200, 300, 400, 500, 400, 300, 200, 400});
            nm.createNotificationChannel(mChannel);

        }
    }


}
