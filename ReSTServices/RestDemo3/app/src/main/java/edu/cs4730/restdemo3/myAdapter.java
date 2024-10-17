package edu.cs4730.restdemo3;

import android.content.Context;
import android.graphics.Bitmap;
import android.graphics.BitmapFactory;

import androidx.annotation.NonNull;
import androidx.appcompat.app.AppCompatActivity;
import androidx.fragment.app.FragmentManager;
import androidx.recyclerview.widget.RecyclerView;

import android.util.Log;
import android.view.LayoutInflater;
import android.view.View;
import android.view.ViewGroup;

import java.io.BufferedInputStream;
import java.io.IOException;
import java.io.InputStream;
import java.net.Authenticator;
import java.net.PasswordAuthentication;

import java.net.URL;
import java.net.URLConnection;
import java.util.ArrayList;

import edu.cs4730.restdemo3.databinding.RowlayoutBinding;

/**
 * this adapter is very similar to the adapters used for listview, except a ViewHolder is required
 * see http://developer.android.com/training/improving-layouts/smooth-scrolling.html
 * except instead having to implement a ViewHolder, it is implemented within
 * the adapter.
 */

class myAdapter extends RecyclerView.Adapter<myAdapter.ViewHolder> {

    private Context mContext;
    private ArrayList<myObj> list = null;
    private PictureFragment myDialog;
    private FragmentManager fm;
    private Bitmap loading;
    private AppCompatActivity act;

    myAdapter(ArrayList<myObj> mlist, AppCompatActivity mAct) {
        list = mlist;
        act = mAct;
        mContext = mAct.getApplicationContext();
        fm = mAct.getSupportFragmentManager();
        myDialog = new PictureFragment();
        loading = BitmapFactory.decodeResource(mContext.getResources(), R.drawable.loading);
    }

    @NonNull
    @Override
    public ViewHolder onCreateViewHolder(@NonNull ViewGroup viewGroup, int i) {
        RowlayoutBinding v = RowlayoutBinding.inflate(LayoutInflater.from(mContext), viewGroup, false);
        return new ViewHolder(v);
    }

    @Override
    public void onBindViewHolder(final ViewHolder viewHolder, int i) {

        viewHolder.id = viewHolder.getAbsoluteAdapterPosition();
        viewHolder.viewBinding.tvTitle.setText(list.get(i).title);
        viewHolder.viewBinding.ivPic.setImageBitmap(loading);
        new Thread(new getPic(list.get(i).piclocation, viewHolder)).start();
        viewHolder.viewBinding.cardview.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View v) {
                myDialog.setpic((Bitmap) viewHolder.viewBinding.ivPic.getTag());
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
        public RowlayoutBinding viewBinding;
        public int id;
        ViewHolder(RowlayoutBinding viewBinding) {
            super(viewBinding.getRoot());
            this.viewBinding = viewBinding;

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
                    viewHolder.viewBinding.ivPic.setTag(bm);
                    viewHolder.viewBinding.ivPic.setImageBitmap(bm);
                }
            });
        }
    }


}
