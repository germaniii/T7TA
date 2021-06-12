package com.example.emav1;

import android.content.Context;
import android.view.LayoutInflater;
import android.view.View;
import android.view.ViewGroup;
import android.widget.TextView;

import androidx.recyclerview.widget.RecyclerView;

import java.util.List;

public class ContactListAdapter extends RecyclerView.Adapter<ContactListAdapter.ViewHolder> {

    private List<String> mName, mNumber;
    private LayoutInflater mInflater;
    private ItemClickListener mClickListener;

    // data is passed into the constructor
    ContactListAdapter(Context context, List<String> name, List<String> number) {
        this.mInflater = LayoutInflater.from(context);
        this.mName = name;
        this.mNumber = number;
    }

    // inflates the row layout from xml when needed
    @Override
    public ViewHolder onCreateViewHolder(ViewGroup parent, int viewType) {
        View view = mInflater.inflate(R.layout.contactlist_row, parent, false);
        return new ViewHolder(view);
    }

    // binds the data to the TextView in each row
    @Override
    public void onBindViewHolder(ViewHolder holder, int position) {
        String name = mName.get(position);
        String numbers = mNumber.get(position);
        holder.TextViewNames.setText(name);
        holder.TextViewNumbers.setText(numbers);
    }

    // total number of rows
    @Override
    public int getItemCount() {
        return mName.size();
    }


    // stores and recycles views as they are scrolled off screen
    public class ViewHolder extends RecyclerView.ViewHolder implements View.OnClickListener {
        TextView TextViewNames, TextViewNumbers;

        ViewHolder(View itemView) {
            super(itemView);
            TextViewNames = itemView.findViewById(R.id.contactName);
            TextViewNumbers = itemView.findViewById(R.id.contactNumber);
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
    String getNumber(int id) {
        return mNumber.get(id);
    }

    // allows clicks events to be caught
    void setClickListener(FragmentContactList itemClickListener) {
        mClickListener = (ContactListAdapter.ItemClickListener) itemClickListener;
    }

    // parent activity will implement this method to respond to click events
    public interface ItemClickListener {
        void onItemClick(View view, int adapterPosition);
    }
}