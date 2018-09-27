package edu.cs4730.restdemo1;


import android.content.Context;
import androidx.cardview.widget.CardView;
import androidx.recyclerview.widget.RecyclerView;
import android.view.LayoutInflater;
import android.view.View;
import android.view.ViewGroup;
import android.widget.TextView;
import android.widget.Toast;

import org.json.JSONArray;
import org.json.JSONException;
import org.json.JSONObject;

/*
 * this adapter is very similar to the adapters used for listview, except a ViewHolder is required
 * see http://developer.android.com/training/improving-layouts/smooth-scrolling.html
 * except instead having to implement a ViewHolder, it is implemented within
 * the adapter.
 */

class myAdapter extends RecyclerView.Adapter<myAdapter.ViewHolder> {

    private JSONArray myList;
    private int rowLayout;
    private Context mContext;

    //JSONObject  jsonRootObject = new JSONObject(strJson);
    myAdapter(JSONArray myList, int rowLayout, Context context) {
        this.myList = myList;
        this.rowLayout = rowLayout;
        this.mContext = context;
    }

    @Override
    public ViewHolder onCreateViewHolder(ViewGroup viewGroup, int i) {
        View v = LayoutInflater.from(viewGroup.getContext()).inflate(rowLayout, viewGroup, false);
        return new ViewHolder(v);
    }

    @Override
    public void onBindViewHolder(final ViewHolder viewHolder, int i) {

        JSONObject entry = null;
        try {
            entry = myList.getJSONObject(i);

            viewHolder.UserID.setText(entry.optString("userId"));
            viewHolder.aID.setText(entry.optString("id"));
            viewHolder.title.setText(entry.optString("title"));
            viewHolder.body.setText(entry.optString("body"));
            viewHolder.cardview.setOnClickListener(new View.OnClickListener() {
                @Override
                public void onClick(View v) {
                    Toast.makeText(mContext, "id is " + viewHolder.aID.getText().toString(), Toast.LENGTH_LONG).show();
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
        TextView UserID;
        TextView aID;
        TextView title;
        TextView body;
        CardView cardview;

        ViewHolder(View itemView) {
            super(itemView);
            UserID = (TextView) itemView.findViewById(R.id.tv_userid);
            aID = (TextView) itemView.findViewById(R.id.tv_aid);
            title = (TextView) itemView.findViewById(R.id.tv_title);
            body = (TextView) itemView.findViewById(R.id.tv_body);
            cardview = (CardView) itemView.findViewById(R.id.cardview);
        }
    }
}
