package edu.cs4730.restdemo3;


import android.app.Activity;
import android.content.Context;
import android.graphics.Bitmap;
import android.graphics.BitmapFactory;
import android.os.AsyncTask;

import androidx.annotation.NonNull;
import androidx.appcompat.app.AppCompatActivity;
import androidx.core.app.ActivityCompat;
import androidx.fragment.app.FragmentManager;
import androidx.cardview.widget.CardView;
import androidx.recyclerview.widget.RecyclerView;

import android.util.Log;
import android.view.LayoutInflater;
import android.view.View;
import android.view.ViewGroup;
import android.widget.ImageView;
import android.widget.TextView;

import java.io.BufferedInputStream;
import java.io.IOException;
import java.io.InputStream;
import java.net.Authenticator;
import java.net.PasswordAuthentication;

import java.net.URI;
import java.net.URL;
import java.net.URLConnection;
import java.util.ArrayList;

/**
 * this adapter is very similar to the adapters used for listview, except a ViewHolder is required
 * see http://developer.android.com/training/improving-layouts/smooth-scrolling.html
 * except instead having to implement a ViewHolder, it is implemented within
 * the adapter.
 */

class myAdapter extends RecyclerView.Adapter<myAdapter.ViewHolder> {

    private int rowLayout;
    private Context mContext;
    private ArrayList<myObj> list = null;
    private PictureFragment myDialog;
    private FragmentManager fm;
    private Bitmap loading;
    private AppCompatActivity act;

    myAdapter(ArrayList<myObj> mlist, int mrowLayout, AppCompatActivity mAct) {
        list = mlist;
        act = mAct;
        rowLayout = mrowLayout;
        mContext = mAct.getApplicationContext();
        fm = mAct.getSupportFragmentManager();
        myDialog = new PictureFragment();
        loading = BitmapFactory.decodeResource(mContext.getResources(), R.drawable.loading);
    }

    @NonNull
    @Override
    public ViewHolder onCreateViewHolder(ViewGroup viewGroup, int i) {
        View v = LayoutInflater.from(viewGroup.getContext()).inflate(rowLayout, viewGroup, false);
        return new ViewHolder(v);
    }

    @Override
    public void onBindViewHolder(final ViewHolder viewHolder, int i) {

        viewHolder.id = i;
        viewHolder.title.setText(list.get(i).title);
        viewHolder.pic.setImageBitmap(loading);
        new Thread(new getPic(list.get(i).piclocation, viewHolder)).start();
        viewHolder.cardview.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View v) {
                myDialog.setpic((Bitmap) viewHolder.pic.getTag());
                myDialog.show(fm, null);
            }
        });

    }

    @Override
    public int getItemCount() {
        return list == null ? 0 : list.size();
    }

    public void setData(ArrayList<myObj> l) {
        list = l;
        notifyDataSetChanged();
    }

    static class ViewHolder extends RecyclerView.ViewHolder {
        public int id;
        public TextView title;
        ImageView pic;
        CardView cardview;

        ViewHolder(View itemView) {
            super(itemView);
            pic = itemView.findViewById(R.id.iv_pic);
            title = itemView.findViewById(R.id.tv_title);
            cardview = itemView.findViewById(R.id.cardview);
        }
    }

    //authenication code used from:
    //http://developer.android.com/reference/java/net/HttpURLConnection.html
    //and http://stackoverflow.com/questions/4883100/how-to-handle-http-authentication-using-httpurlconnection
    private static final String kuser = "user1"; // your account name
    private static final String kpass = "android"; // your password for the account

    static class MyAuthenticator extends Authenticator {
        public PasswordAuthentication getPasswordAuthentication() {
            // I haven't checked getRequestingScheme() here, since for NTLM
            // and Negotiate, the usrname and password are all the same.
            // System.err.println("Feeding username and password for " + getRequestingScheme());
            return (new PasswordAuthentication(kuser, kpass.toCharArray()));
        }
    }

    private class getPic implements Runnable {
        ViewHolder viewHolder;
        String url;
        String TAG = "getPic";

        getPic(String url, ViewHolder viewHolder) {
            this.url = url;
            this.viewHolder = viewHolder;
        }

        public void run() {
            Bitmap bm = null;
            try {
                //setup password authentication
                Authenticator.setDefault(new MyAuthenticator());
                URL aURL = new URL(url);
                URLConnection conn = aURL.openConnection();
                conn.connect();
                InputStream is = conn.getInputStream();
                BufferedInputStream bis = new BufferedInputStream(is);
                bm = BitmapFactory.decodeStream(bis);
                bis.close();
                is.close();
            } catch (IOException e) {
                Log.e(TAG, "Error getting bitmap", e);
            }
            onPostExecute(bm);
        }


        public void onPostExecute(Bitmap bm) {
            act.runOnUiThread(new Runnable() {
                @Override
                public void run() {
                    viewHolder.pic.setTag(bm);
                    viewHolder.pic.setImageBitmap(bm);
                }
            });
        }
    }


}
