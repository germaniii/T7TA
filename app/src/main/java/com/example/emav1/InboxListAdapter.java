package com.example.emav1;

import android.content.Context;
import android.view.LayoutInflater;
import android.view.View;
import android.view.ViewGroup;
import android.widget.TextView;

import androidx.recyclerview.widget.RecyclerView;

import java.util.List;

public class InboxListAdapter extends RecyclerView.Adapter<InboxListAdapter.ViewHolder> {

    private List<String> mName, mNumber, mMessage;
    private LayoutInflater mInflater;
    private ItemClickListener mClickListener;

    // data is passed into the constructor
    InboxListAdapter(Context context, List<String> name, List<String> number, List<String> message) {
        this.mInflater = LayoutInflater.from(context);
        this.mName = name;
        this.mNumber = number;
        this.mMessage = message;
    }

    // inflates the row layout from xml when needed
    @Override
    public ViewHolder onCreateViewHolder(ViewGroup parent, int viewType) {
        View view = mInflater.inflate(R.layout.inboxlist_row, parent, false);
        return new ViewHolder(view);
    }

    // binds the data to the TextView in each row
    @Override
    public void onBindViewHolder(ViewHolder holder, int position) {
        String name = mName.get(position);
        String number = mNumber.get(position);
        String message = mMessage.get(position);
        holder.TextViewName.setText(name);
        holder.TextViewNumber.setText(number);
        holder.TextViewMessage.setText(message);
    }

    // total number of rows
    @Override
    public int getItemCount() {
        return mName.size();
    }


    // stores and recycles views as they are scrolled off screen
    public class ViewHolder extends RecyclerView.ViewHolder implements View.OnClickListener {
        TextView TextViewName, TextViewNumber, TextViewMessage;

        ViewHolder(View itemView) {
            super(itemView);
            TextViewName = itemView.findViewById(R.id.inboxName);
            TextViewNumber = itemView.findViewById(R.id.inboxNumber);
            TextViewMessage = itemView.findViewById(R.id.inboxMessage);
            itemView.setOnClickListener(this);
        }

        @Override
        public void onClick(View view) {
            if (mClickListener != null) mClickListener.onItemClick(view, getAdapterPosition());
        }
    }

    // convenience method for getting data at click position
    String getName(int id) {
        return mName.get(id);
    }

    // allows clicks events to be caught
    void setClickListener(MainActivity itemClickListener) {
        mClickListener = (ItemClickListener) itemClickListener;
    }

    // parent activity will implement this method to respond to click events
    public interface ItemClickListener {
        void onItemClick(View view, int adapterPosition);
    }
}
