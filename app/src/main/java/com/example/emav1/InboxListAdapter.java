package com.example.emav1;

import android.content.Context;
import android.view.LayoutInflater;
import android.view.View;
import android.view.ViewGroup;
import android.widget.TextView;

import androidx.recyclerview.widget.RecyclerView;

import java.util.List;

public class InboxListAdapter extends RecyclerView.Adapter<InboxListAdapter.ViewHolder> {

    private List<String> mName, mNumber, mMessage, mReceived, mSent;
    private LayoutInflater mInflater;
    private ItemClickListener mClickListener;

    // data is passed into the constructor
    InboxListAdapter(Context context, List<String> messageNames, List<String> messageNum, List<String> messageText,
                     List<String> messageReceived, List<String> messageSent) {
        this.mInflater = LayoutInflater.from(context);
        this.mName = messageNames;
        this.mNumber = messageNum;
        this.mMessage = messageText;
        this.mReceived = messageReceived;
        this.mSent = messageSent;
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
        String received = mReceived.get(position);
        String sent = mSent.get(position);

        holder.TextViewName.setText(name);
        holder.TextViewNumber.setText(number);
        holder.TextViewMessage.setText(message);
        if(sent == null) holder.TextViewDate.setText(received);
        else holder.TextViewDate.setText(sent);
    }

    // total number of rows
    @Override
    public int getItemCount() {
        return mMessage.size();
    }


    // stores and recycles views as they are scrolled off screen
    public class ViewHolder extends RecyclerView.ViewHolder implements View.OnClickListener {
        TextView TextViewName, TextViewNumber, TextViewMessage, TextViewDate;

        ViewHolder(View itemView) {
            super(itemView);
            itemView.setOnClickListener(this);
            TextViewName = itemView.findViewById(R.id.inboxName);
            TextViewNumber = itemView.findViewById(R.id.inboxNumber);
            TextViewMessage = itemView.findViewById(R.id.inboxMessage);
            TextViewDate = itemView.findViewById(R.id.inboxDate);
        }

        @Override
        public void onClick(View view) {
            if (mClickListener != null) mClickListener.onItemClick(view, getAdapterPosition());
        }
    }

    // convenience method for getting data at click position
    String getName(int id) {
        return mMessage.get(id);
    }

    // allows clicks events to be caught
    void setClickListener(FragmentMain itemClickListener) {
        mClickListener = (FragmentMain) itemClickListener;
    }

    // parent activity will implement this method to respond to click events
    public interface ItemClickListener {
        void onItemClick(View view, int adapterPosition);
    }
}
