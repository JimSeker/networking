package edu.cs4730.restdemo1;

import android.content.Context;

import androidx.annotation.NonNull;
import androidx.recyclerview.widget.RecyclerView;

import android.view.LayoutInflater;
import android.view.View;
import android.view.ViewGroup;
import android.widget.Toast;

import org.json.JSONArray;
import org.json.JSONException;
import org.json.JSONObject;

import edu.cs4730.restdemo1.databinding.RowlayoutBinding;

/**
 * this adapter is very similar to the adapters used for listview, except a ViewHolder is required
 * see http://developer.android.com/training/improving-layouts/smooth-scrolling.html
 * except instead having to implement a ViewHolder, it is implemented within
 * the adapter.
 */

class myAdapter extends RecyclerView.Adapter<myAdapter.ViewHolder> {

    private JSONArray myList;
    private Context mContext;

    myAdapter(JSONArray myList, Context context) {
        this.myList = myList;
        this.mContext = context;
    }

    @NonNull
    @Override
    public ViewHolder onCreateViewHolder(@NonNull ViewGroup viewGroup, int i) {
        RowlayoutBinding v = RowlayoutBinding.inflate(LayoutInflater.from(mContext), viewGroup, false);
        return new ViewHolder(v);
    }

    @Override
    public void onBindViewHolder(final ViewHolder viewHolder, int i) {

        JSONObject entry = null;
        try {
            entry = myList.getJSONObject(i);
            viewHolder.viewBinding.tvUserid.setText(entry.optString("userId"));
            viewHolder.viewBinding.tvAid.setText(entry.optString("id"));
            viewHolder.viewBinding.tvTitle.setText(entry.optString("title"));
            viewHolder.viewBinding.tvBody.setText(entry.optString("body"));
            viewHolder.viewBinding.cardview.setOnClickListener(new View.OnClickListener() {
                @Override
                public void onClick(View v) {
                    Toast.makeText(mContext, "id is " + viewHolder.viewBinding.tvAid.getText().toString(), Toast.LENGTH_LONG).show();
                }
            });
        } catch (JSONException e) {
            e.printStackTrace();
        }

    }

    @Override
    public int getItemCount() {
        return myList == null ? 0 : myList.length();
    }

    public void setData(JSONArray list) {
        myList = list;
        notifyDataSetChanged();
    }

    static class ViewHolder extends RecyclerView.ViewHolder {
        public RowlayoutBinding viewBinding;

        ViewHolder(RowlayoutBinding viewBinding) {
            super(viewBinding.getRoot());
            this.viewBinding = viewBinding;

        }
    }
}
