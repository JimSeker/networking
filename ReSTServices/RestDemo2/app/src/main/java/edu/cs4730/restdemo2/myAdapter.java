package edu.cs4730.restdemo2;


import android.content.Context;
import android.support.v7.widget.CardView;
import android.support.v7.widget.RecyclerView;
import android.view.LayoutInflater;
import android.view.View;
import android.view.ViewGroup;
import android.widget.TextView;
import android.widget.Toast;


import java.util.ArrayList;

/*
 * this adapter is very similar to the adapters used for listview, except a ViewHolder is required
 * see http://developer.android.com/training/improving-layouts/smooth-scrolling.html
 * except instead having to implement a ViewHolder, it is implemented within
 * the adapter.
 */

public class myAdapter extends RecyclerView.Adapter<myAdapter.ViewHolder> {

    private ArrayList<myObj> myList;
    private int rowLayout;
    private Context mContext;

    // Define listener member variable
    private OnItemClickListener listener;
    // Define the listener interface
    public interface OnItemClickListener {
        void onItemClick(String mid, String mtitle, String mbody);
    }
    // Define the method that allows the parent activity or fragment to define the listener
    public void setOnItemClickListener(OnItemClickListener listener) {
        this.listener = listener;
    }


    public myAdapter(ArrayList<myObj> myList, int rowLayout, Context context) {
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

        myObj entry;
        entry = myList.get(i);

        viewHolder.tvID.setText(String.valueOf(entry.id));
        viewHolder.title.setText(entry.title);
        viewHolder.body.setText(entry.body);
        viewHolder.cardview.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View v) {
                if (listener != null) {
                    listener.onItemClick(viewHolder.tvID.getText().toString(), viewHolder.title.getText().toString(), viewHolder.body.getText().toString());
                }

                //Toast.makeText(mContext, "id is " + viewHolder.tvID.getText().toString(), Toast.LENGTH_LONG).show();
            }
        });


    }

    @Override
    public int getItemCount() {
        return myList == null ? 0 : myList.size();
    }

    public void setData(ArrayList<myObj> list) {
        myList = list;
        notifyDataSetChanged();
    }

    public static class ViewHolder extends RecyclerView.ViewHolder {
        public TextView tvID;
        public TextView title;
        public TextView body;
        public CardView cardview;

        public ViewHolder(View itemView) {
            super(itemView);
            tvID = (TextView) itemView.findViewById(R.id.tv_id);
            title = (TextView) itemView.findViewById(R.id.tv_title);
            body = (TextView) itemView.findViewById(R.id.tv_body);
            cardview = (CardView) itemView.findViewById(R.id.cardview);
        }
    }
}
