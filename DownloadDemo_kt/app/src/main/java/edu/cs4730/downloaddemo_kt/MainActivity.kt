package edu.cs4730.downloaddemo_kt

import android.Manifest
import android.annotation.SuppressLint
import android.app.DownloadManager
import android.app.NotificationChannel
import android.app.NotificationManager
import android.content.BroadcastReceiver
import android.content.Context
import android.content.Intent
import android.content.IntentFilter
import android.content.pm.PackageManager
import android.graphics.Color
import android.os.Build
import android.os.Bundle
import android.os.Environment
import android.util.Log
import android.widget.Toast
import androidx.activity.result.ActivityResultLauncher
import androidx.activity.result.contract.ActivityResultContracts
import androidx.appcompat.app.AppCompatActivity
import androidx.core.content.ContextCompat
import androidx.core.view.ViewCompat
import androidx.core.view.WindowInsetsCompat
import edu.cs4730.downloaddemo_kt.databinding.ActivityMainBinding
import java.io.FileNotFoundException
import androidx.core.net.toUri

class MainActivity : AppCompatActivity() {

    var id: String = "test_channel_01"

    //var Download_path: String ="https://sample-videos.com/img/Sample-jpg-image-30mb.jpg";  //even bigger file.  no notification until completed.
    //big file, takes about 30 seconds to download
    var Download_path: String =
        "http://www.learningcontainer.com/wp-content/uploads/2020/07/Large-Sample-Image-download-for-Testing.jpg"
    var Download_filename: String = "extrlargesample.jpg"

    //smaller test file.
    //var  Download_path: String = "http://www.cs.uwyo.edu/~seker/courses/2150/30mbHD.jpg";
    //var  Download_filename: String = "30mbHD.jpg";
    var download_id: Long = -1

    //SharedPreferences preferenceManager;
    lateinit var downloadManager: DownloadManager
    var TAG: String = "MainActivity"
    lateinit var binding: ActivityMainBinding

    lateinit var rpl: ActivityResultLauncher<Array<String>>
    private lateinit var REQUIRED_PERMISSIONS: Array<String>


    override fun onCreate(savedInstanceState: Bundle?) {
        super.onCreate(savedInstanceState)
        binding = ActivityMainBinding.inflate(layoutInflater)
        setContentView(binding.getRoot())
        ViewCompat.setOnApplyWindowInsetsListener(binding.main) { v, insets ->
            val systemBars = insets.getInsets(WindowInsetsCompat.Type.systemBars())
            v.setPadding(systemBars.left, systemBars.top, systemBars.right, systemBars.bottom)
            WindowInsetsCompat.CONSUMED
        }

        if (Build.VERSION.SDK_INT >= Build.VERSION_CODES.TIRAMISU) {  //For API 33+
            REQUIRED_PERMISSIONS = arrayOf(
                Manifest.permission.ACCESS_MEDIA_LOCATION,
                Manifest.permission.POST_NOTIFICATIONS
            )
        } else if (Build.VERSION.SDK_INT >= Build.VERSION_CODES.Q) {  //For API 29+ (q) to 32.
            REQUIRED_PERMISSIONS = arrayOf(Manifest.permission.ACCESS_MEDIA_LOCATION)
        } else { //for 26 to 28.
            REQUIRED_PERMISSIONS = arrayOf(Manifest.permission.WRITE_EXTERNAL_STORAGE)
        }


        //Use this to check permissions.
        rpl = registerForActivityResult(
            ActivityResultContracts.RequestMultiplePermissions()
        ) { isGranted ->
            var granted = true
            for ((key, value) in isGranted) {
                logthis("$key is $value")
                if (!value) granted = false
            }
            if (granted) logthis("all permissions granted.")
            else {
                Toast.makeText(
                    applicationContext,
                    "Permissions not granted by the user.",
                    Toast.LENGTH_SHORT
                ).show()
                finish()
            }
        }

        downloadManager = getSystemService(DOWNLOAD_SERVICE) as DownloadManager

        binding.download.setOnClickListener { downloadfile() }
        binding.download2.setOnClickListener { downloadfilenonoti() }
        createchannel()
    }

    fun downloadfile() {
        if (!allPermissionsGranted()) {
            rpl.launch(REQUIRED_PERMISSIONS)
            return
        }

        //setup download and how it will look on the notification bar.
        val Download_Uri = Download_path.toUri()
        val request = DownloadManager.Request(Download_Uri)
            .setAllowedNetworkTypes(DownloadManager.Request.NETWORK_WIFI or DownloadManager.Request.NETWORK_MOBILE)
            .setAllowedOverRoaming(false)
            .setTitle(Download_filename)
            .setDescription("Video") //show while downloading, and completed.  but doesn't appear while downloading.  may just be broken at this point.
            .setNotificationVisibility(DownloadManager.Request.VISIBILITY_VISIBLE) //only when completed.
            //.setNotificationVisibility(DownloadManager.Request.VISIBILITY_VISIBLE_NOTIFY_ONLY_COMPLETION)
            .setDestinationInExternalPublicDir(Environment.DIRECTORY_DOWNLOADS, Download_filename)
        //request.allowScanningByMediaScanner(); //depreciated.  always scans downloads now.
        //for inside the app space, use
        //.setDestinationInExternalFilesDir(MainActivity.this, Environment.DIRECTORY_DOWNLOADS,Download_filename);
        download_id = downloadManager.enqueue(request)
    }

    fun downloadfilenonoti() {
        if (!allPermissionsGranted()) {
            rpl.launch(REQUIRED_PERMISSIONS)
            return
        }
        //This should down the file without creating a notification.  if you don't have permission (in manifest) it will crash.
        val Download_Uri = Download_path.toUri()

        val request = DownloadManager.Request(Download_Uri)
            .setAllowedNetworkTypes(DownloadManager.Request.NETWORK_WIFI) //.setAllowedNetworkTypes(DownloadManager.Request.NETWORK_WIFI | DownloadManager.Request.NETWORK_MOBILE);
            //.setAllowedOverRoaming(true)
            .setNotificationVisibility(DownloadManager.Request.VISIBILITY_HIDDEN) //api 11 and above!
            .setDestinationInExternalPublicDir(Environment.DIRECTORY_DOWNLOADS, Download_filename)
        //request.allowScanningByMediaScanner();
        download_id = downloadManager.enqueue(request)
    }

    fun logthis(item: String) {
        if (!item.isEmpty()) {
            binding.logger.append(item + "\n")
            Log.w(TAG, item)
        }
    }

    /**
     * for API 26+ create notification channels
     */
    private fun createchannel() {
        val nm = getSystemService(NOTIFICATION_SERVICE) as NotificationManager
        val mChannel = NotificationChannel(
            id,
            getString(R.string.channel_name),  //name of the channel
            NotificationManager.IMPORTANCE_DEFAULT
        ) //importance level
        //important level: default is is high on the phone.  high is urgent on the phone.  low is medium, so none is low?
        // Configure the notification channel.
        mChannel.description = getString(R.string.channel_description)
        mChannel.enableLights(true)
        //Sets the notification light color for notifications posted to this channel, if the device supports this feature.
        mChannel.lightColor = Color.RED
        mChannel.enableVibration(true)
        mChannel.setShowBadge(true)
        mChannel.setVibrationPattern(longArrayOf(100, 200, 300, 400, 500, 400, 300, 200, 400))
        nm.createNotificationChannel(mChannel)
    }

    /**
     * This a helper method to check for the permissions.
     */
    private fun allPermissionsGranted(): Boolean {
        for (permission in REQUIRED_PERMISSIONS) {
            if (ContextCompat.checkSelfPermission(
                    this,
                    permission
                ) != PackageManager.PERMISSION_GRANTED
            ) {
                return false
            }
        }
        return true
    }


    @SuppressLint("UnspecifiedRegisterReceiverFlag") //lent, learn if statements.
    override fun onResume() {
        super.onResume()
        val intentFilter = IntentFilter(DownloadManager.ACTION_DOWNLOAD_COMPLETE)
        if (Build.VERSION.SDK_INT >= Build.VERSION_CODES.TIRAMISU) {  //For API 33+
            registerReceiver(downloadReceiver, intentFilter, RECEIVER_EXPORTED)
        } else {
            registerReceiver(downloadReceiver, intentFilter)
        }
    }

    override fun onPause() {
        super.onPause()
        unregisterReceiver(downloadReceiver)
    }

    private val downloadReceiver: BroadcastReceiver = object : BroadcastReceiver() {
        var intentdownloadId: Long = 0

        override fun onReceive(context: Context?, intent: Intent) {
            val action = intent.action
            if (DownloadManager.ACTION_DOWNLOAD_COMPLETE == action) {
                val extras = intent.extras
                intentdownloadId = extras!!.getLong(DownloadManager.EXTRA_DOWNLOAD_ID, -1)
                logthis("should match: id is $download_id int_id is $intentdownloadId")
            }

            val query = DownloadManager.Query()
            query.setFilterById(intentdownloadId)
            val cursor = downloadManager.query(query)

            if (cursor.moveToFirst()) {
                val columnIndex = cursor.getColumnIndex(DownloadManager.COLUMN_STATUS)
                val status = cursor.getInt(columnIndex)
                val columnReason = cursor.getColumnIndex(DownloadManager.COLUMN_REASON)
                val reason = cursor.getInt(columnReason)
                val columnURI = cursor.getColumnIndex(DownloadManager.COLUMN_LOCAL_URI)
                val Fname: String?
                if (columnURI >= 0) {
                    Fname = cursor.getString(columnURI)
                } else {
                    Fname = "unknown"
                }
                if (status == DownloadManager.STATUS_SUCCESSFUL) {
                    try {
                        val file = downloadManager.openDownloadedFile(intentdownloadId)
                        //Toast.makeText(MainActivity.this, "File Downloaded: " + Fname + " and ready to process", Toast.LENGTH_LONG).show();
                        logthis("File Downloaded: $Fname and ready to process")
                    } catch (e: FileNotFoundException) {
                        e.printStackTrace()
                        //Toast.makeText(MainActivity.this, e.toString(), Toast.LENGTH_LONG).show();
                        e.printStackTrace()
                    }
                } else if (status == DownloadManager.STATUS_FAILED) {
                    // Toast.makeText(MainActivity.this,"FAILED!\n" + "reason of " + reason,  Toast.LENGTH_LONG).show();
                    logthis("FAILED!\nreason of $reason")
                } else if (status == DownloadManager.STATUS_PAUSED) {
                    // Toast.makeText(MainActivity.this,"PAUSED!\n" + "reason of " + reason,  Toast.LENGTH_LONG).show();
                    logthis("PAUSED!\nreason of $reason")
                } else if (status == DownloadManager.STATUS_PENDING) {
                    // Toast.makeText(MainActivity.this,"PENDING!", Toast.LENGTH_LONG).show();
                    logthis("PENDING!")
                } else if (status == DownloadManager.STATUS_RUNNING) {
                    // Toast.makeText(MainActivity.this,"RUNNING!", Toast.LENGTH_LONG).show();
                    logthis("RUNNING!")
                }
            }
        }
    }


}