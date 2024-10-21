package edu.cs4730.downloaddemo;

import java.io.FileNotFoundException;
import java.util.Map;

import android.Manifest;
import android.annotation.SuppressLint;
import android.app.NotificationChannel;
import android.app.NotificationManager;
import android.content.pm.PackageManager;
import android.graphics.Color;
import android.net.Uri;
import android.os.Build;
import android.os.Bundle;
import android.os.Environment;
import android.os.ParcelFileDescriptor;
import android.app.DownloadManager;
import android.content.BroadcastReceiver;
import android.content.Context;
import android.content.Intent;
import android.content.IntentFilter;
import android.database.Cursor;

import androidx.activity.result.ActivityResultCallback;
import androidx.activity.result.ActivityResultLauncher;
import androidx.activity.result.contract.ActivityResultContracts;
import androidx.appcompat.app.AppCompatActivity;
import androidx.core.content.ContextCompat;
import androidx.core.graphics.Insets;
import androidx.core.view.ViewCompat;
import androidx.core.view.WindowInsetsCompat;

import android.util.Log;
import android.view.View;
import android.widget.Button;
import android.widget.Toast;

import edu.cs4730.downloaddemo.databinding.ActivityMainBinding;

/**
 * This is a simple example of how to use the download manager.  except not really that simple anymore with all permissions needed.
 * Also I need a much larger file, even 15MB is just to quick.  I don't seem to be able to get an on going notification anymore.
 * using https://www.learningcontainer.com/sample-jpeg-file-download-for-testing/
 */


public class MainActivity extends AppCompatActivity {
    //for the channel id notification.
    public static String id = "test_channel_01";

    //String Download_path ="https://sample-videos.com/img/Sample-jpg-image-30mb.jpg";  //even bigger file.  no notification until completed.
    //big file, takes about 30 seconds to download
    String Download_path = "http://www.learningcontainer.com/wp-content/uploads/2020/07/Large-Sample-Image-download-for-Testing.jpg";
    String Download_filename = "extrlargesample.jpg";

    //smaller test file.
    //String Download_path = "http://www.cs.uwyo.edu/~seker/courses/2150/30mbHD.jpg";
    //String Download_filename = "30mbHD.jpg";
    long download_id = -1;

    //SharedPreferences preferenceManager;
    DownloadManager downloadManager;
    String TAG = "MainActivity";
    ActivityMainBinding binding;

    ActivityResultLauncher<String[]> rpl;
    private String[] REQUIRED_PERMISSIONS;
    /**
     * Called when the activity is first created.
     */
    @Override
    public void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        binding = ActivityMainBinding.inflate(getLayoutInflater());
        setContentView(binding.getRoot());
        ViewCompat.setOnApplyWindowInsetsListener(binding.main, (v, insets) -> {
            Insets systemBars = insets.getInsets(WindowInsetsCompat.Type.systemBars());
            v.setPadding(systemBars.left, systemBars.top, systemBars.right, systemBars.bottom);
            return insets;
        });


        if (Build.VERSION.SDK_INT >= Build.VERSION_CODES.TIRAMISU) {  //For API 33+
            REQUIRED_PERMISSIONS = new String[]{Manifest.permission.ACCESS_MEDIA_LOCATION, Manifest.permission.READ_MEDIA_IMAGES,Manifest.permission.POST_NOTIFICATIONS};
        } else if (Build.VERSION.SDK_INT >= Build.VERSION_CODES.Q) {  //For API 29+ (q) to 32.
            REQUIRED_PERMISSIONS = new String[]{Manifest.permission.ACCESS_MEDIA_LOCATION};
        } else { //for 26 to 28.
            REQUIRED_PERMISSIONS = new String[]{Manifest.permission.WRITE_EXTERNAL_STORAGE};
        }

        //Use this to check permissions.
        rpl = registerForActivityResult(new ActivityResultContracts.RequestMultiplePermissions(),
            new ActivityResultCallback<Map<String, Boolean>>() {
                @Override
                public void onActivityResult(Map<String, Boolean> isGranted) {
                    if (allPermissionsGranted()) {
                        for (Map.Entry<String, Boolean> x : isGranted.entrySet())
                            logthis(x.getKey() + " is " + x.getValue());
                    } else {
                        Toast.makeText(getApplicationContext(), "Permissions not granted by the user.", Toast.LENGTH_SHORT).show();
                        finish();
                    }
                }
            }
        );

        downloadManager = (DownloadManager) getSystemService(DOWNLOAD_SERVICE);

        findViewById(R.id.download).setOnClickListener(new Button.OnClickListener() {
            @Override
            public void onClick(View arg0) {

                downloadfile();

            }
        });
        findViewById(R.id.download2).setOnClickListener(new Button.OnClickListener() {
            @Override
            public void onClick(View arg0) {
                downloadfilenonoti();
            }
        });
        createchannel();
    }

    void downloadfile() {
        if (!allPermissionsGranted()) {
            rpl.launch(REQUIRED_PERMISSIONS);
            return;
        }

        //setup download and how it will look on the notification bar.
        Uri Download_Uri = Uri.parse(Download_path);
        DownloadManager.Request request = new DownloadManager.Request(Download_Uri)
            .setAllowedNetworkTypes(DownloadManager.Request.NETWORK_WIFI | DownloadManager.Request.NETWORK_MOBILE)
            .setAllowedOverRoaming(false)
            .setTitle(Download_filename)
            .setDescription("Video")
            //show while downloading, and completed.  but doesn't appear while downloading.  may just be broken at this point.
            .setNotificationVisibility(DownloadManager.Request.VISIBILITY_VISIBLE)
            //only when completed.
            //.setNotificationVisibility(DownloadManager.Request.VISIBILITY_VISIBLE_NOTIFY_ONLY_COMPLETION)
            .setDestinationInExternalPublicDir(Environment.DIRECTORY_DOWNLOADS, Download_filename);
        //request.allowScanningByMediaScanner(); //depreciated.  always scans downloads now.
        //for inside the app space, use
        //.setDestinationInExternalFilesDir(MainActivity.this, Environment.DIRECTORY_DOWNLOADS,Download_filename);
        download_id = downloadManager.enqueue(request);
    }

    void downloadfilenonoti() {
        if (!allPermissionsGranted()) {
            rpl.launch(REQUIRED_PERMISSIONS);
            return;
        }
        //This should down the file without creating a notification.  if you don't have permission (in manifest) it will crash.
        Uri Download_Uri = Uri.parse(Download_path);

        DownloadManager.Request request = new DownloadManager.Request(Download_Uri)
            .setAllowedNetworkTypes(DownloadManager.Request.NETWORK_WIFI)
            //.setAllowedNetworkTypes(DownloadManager.Request.NETWORK_WIFI | DownloadManager.Request.NETWORK_MOBILE);
            //.setAllowedOverRoaming(true)
            .setNotificationVisibility(DownloadManager.Request.VISIBILITY_HIDDEN)  //api 11 and above!
            .setDestinationInExternalPublicDir(Environment.DIRECTORY_DOWNLOADS, Download_filename);
        //request.allowScanningByMediaScanner();
        download_id = downloadManager.enqueue(request);
    }



    @SuppressLint("UnspecifiedRegisterReceiverFlag") //lent, learn if statements.
    @Override
    protected void onResume() {
        super.onResume();
        IntentFilter intentFilter = new IntentFilter(DownloadManager.ACTION_DOWNLOAD_COMPLETE);
        if (Build.VERSION.SDK_INT >= Build.VERSION_CODES.TIRAMISU) {  //For API 33+
            registerReceiver(downloadReceiver, intentFilter, Context.RECEIVER_EXPORTED);
        } else {
            registerReceiver(downloadReceiver, intentFilter);
        }
    }

    @Override
    protected void onPause() {
        super.onPause();
        unregisterReceiver(downloadReceiver);
    }

    private final BroadcastReceiver downloadReceiver = new BroadcastReceiver() {
        long intentdownloadId;

        @Override
        public void onReceive(Context context, Intent intent) {
            String action = intent.getAction();
            if (DownloadManager.ACTION_DOWNLOAD_COMPLETE.equals(action)) {
                Bundle extras = intent.getExtras();
                intentdownloadId = extras.getLong(DownloadManager.EXTRA_DOWNLOAD_ID, -1);
                logthis("should match: id is " + download_id + " int_id is " + intentdownloadId);
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
                        //Toast.makeText(MainActivity.this, "File Downloaded: " + Fname + " and ready to process", Toast.LENGTH_LONG).show();
                        logthis("File Downloaded: " + Fname + " and ready to process");
                    } catch (FileNotFoundException e) {
                        e.printStackTrace();
                        //Toast.makeText(MainActivity.this, e.toString(), Toast.LENGTH_LONG).show();
                        e.printStackTrace();
                    }

                } else if (status == DownloadManager.STATUS_FAILED) {
                    // Toast.makeText(MainActivity.this,"FAILED!\n" + "reason of " + reason,  Toast.LENGTH_LONG).show();
                    logthis("FAILED!\n" + "reason of " + reason);
                } else if (status == DownloadManager.STATUS_PAUSED) {
                    // Toast.makeText(MainActivity.this,"PAUSED!\n" + "reason of " + reason,  Toast.LENGTH_LONG).show();
                    logthis("PAUSED!\n" + "reason of " + reason);
                } else if (status == DownloadManager.STATUS_PENDING) {
                    // Toast.makeText(MainActivity.this,"PENDING!", Toast.LENGTH_LONG).show();
                    logthis("PENDING!");
                } else if (status == DownloadManager.STATUS_RUNNING) {
                    // Toast.makeText(MainActivity.this,"RUNNING!", Toast.LENGTH_LONG).show();
                    logthis("RUNNING!");
                }
            }
        }

    };

    public void logthis(String item) {
        if (!item.isEmpty()) {
            binding.logger.append(item + "\n");
            Log.w(TAG, item);
        }
    }

    /**
     * for API 26+ create notification channels
     */
    private void createchannel() {
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

    /**
     * This a helper method to check for the permissions.
     */
    private boolean allPermissionsGranted() {
        for (String permission : REQUIRED_PERMISSIONS) {
            if (ContextCompat.checkSelfPermission(this, permission) != PackageManager.PERMISSION_GRANTED) {
                return false;
            }
        }
        return true;
    }
}