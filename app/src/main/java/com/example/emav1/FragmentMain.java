package com.example.emav1;

import android.app.AlertDialog;
import android.content.Context;
import android.content.DialogInterface;
import android.database.Cursor;
import android.icu.text.SimpleDateFormat;
import android.icu.util.Calendar;
import android.os.Build;
import android.os.Bundle;

import androidx.annotation.NonNull;
import androidx.annotation.Nullable;
import androidx.fragment.app.Fragment;
import androidx.recyclerview.widget.LinearLayoutManager;
import androidx.recyclerview.widget.RecyclerView;

import android.view.LayoutInflater;
import android.view.View;
import android.view.ViewGroup;
import android.widget.EditText;
import android.widget.TextView;
import android.widget.Toast;

import java.util.ArrayList;
import java.util.Date;

public class FragmentMain extends Fragment  implements InboxListAdapter.ItemClickListener{

    View view;

    static InboxListAdapter inboxListAdapter;
    static ArrayList<String> messageID, messageNames,messageNum,messageText,messageReceived, messageSent;
    RecyclerView recyclerView;
    TextView dialog_name, dialog_num, dialog_mess, dialog_date;
    EditText uName, uNumber;
    static Date date;
    static SimpleDateFormat dateFormat;
    static DataBaseHelper dataBaseHelper;

    Context context;

    @Override
    public View onCreateView(LayoutInflater inflater, ViewGroup container, Bundle savedInstanceState) {
        this.context = getActivity();
        // Inflate the layout for this fragment
        return inflater.inflate(R.layout.fragment_main, container, false);
    }

    @Override
    public void onViewCreated(@NonNull View view, @Nullable @org.jetbrains.annotations.Nullable Bundle savedInstanceState) {
        super.onViewCreated(view, savedInstanceState);


        if (Build.VERSION.SDK_INT >= Build.VERSION_CODES.N) {
            date = Calendar.getInstance().getTime();
            dateFormat = new SimpleDateFormat("hh:mm mm-dd-yyyy");
        }

        // data to populate the RecyclerView with
        messageID = new ArrayList<>();
        messageNames = new ArrayList<>();
        messageNum = new ArrayList<>();
        messageText = new ArrayList<>();
        messageSent = new ArrayList<>();
        messageReceived = new ArrayList<>();

        dataBaseHelper = new DataBaseHelper(context);
        storeDBtoArrays();

        // set up the RecyclerView
        recyclerView = getActivity().findViewById(R.id.main_inboxList);
        recyclerView.setLayoutManager(new LinearLayoutManager(context));
        inboxListAdapter = new InboxListAdapter(context, messageNames, messageNum, messageText, messageReceived, messageSent);
        inboxListAdapter.setClickListener(this);
        recyclerView.setAdapter(inboxListAdapter);

        //Checks if Contacts is Empty, if yes, will ask for user's contact number(last 4 digits)
        if(!dataBaseHelper.readAllDataContactsTable().moveToFirst()){
            AlertDialog.Builder builder = new AlertDialog.Builder(context);
            builder.setTitle("User Set-up")
                    .setMessage("\nPlease input your name and the last 4 digits of your phone number.");
            // I'm using fragment here so I'm using getView() to provide ViewGroup
            // but you can provide here any other instance of ViewGroup from your Fragment / Activity
            View viewInflated = LayoutInflater.from(context).inflate(R.layout.dialog_usercontact, (ViewGroup) getActivity().findViewById(android.R.id.content), false);
            // Set up the input
            //final EditText input = (EditText) viewInflated.findViewById(R.id.input);
            uName = (EditText) viewInflated.findViewById(R.id.dialog_uName);
            uNumber = (EditText) viewInflated.findViewById(R.id.dialog_uNumber);
            builder.setView(viewInflated);

            // Set up the buttons
            builder.setPositiveButton("Confirm", new DialogInterface.OnClickListener() {
                @Override
                public void onClick(DialogInterface dialog, int which) {
                    if(uName.getText().toString() == "" || uNumber.getText().toString() == ""){
                        Toast.makeText(context, "Please fill up all fields!", Toast.LENGTH_SHORT).show();
                    }else{
                        String strDate = null;
                        if (android.os.Build.VERSION.SDK_INT >= android.os.Build.VERSION_CODES.N) {
                            strDate = dateFormat.format(date);
                        }
                        String uNameString = uName.getText().toString().trim();
                        uNameString += " (My Number)";
                        dataBaseHelper.addOneContact(uNameString, uNumber.getText().toString(), "");
                        dataBaseHelper.addOneMessage(uNumber.getText().toString(), "This is a test Message!", strDate, "");

                        //refill the contact Array lists so that the Contact ID will be filled with the new information
                        storeDBtoArrays();
                        //Redisplay the list
                        inboxListAdapter.notifyDataSetChanged();
                    }
                }
            });

            builder.show();
        }

    }

    // This function is used to update the inboxlist whenever the DB changes.
    public static void storeDBtoArrays(){
        Cursor cursor = dataBaseHelper.readAllDataMessagesTable();
        Cursor num;
        if(cursor.getCount() == 0){
        }else{
            while (cursor.moveToNext()){
                messageID.add(cursor.getString(0));     //ID
                messageNum.add(cursor.getString(1));    //Number

                // Get Name from Contact Table
                // num is a cursor for contact table. need to put movetoFirst() since first index
                // of a cursor is always -1, giving us errors
                num = dataBaseHelper.readContactName(cursor.getString(1));
                if(num.moveToFirst() && cursor.getCount() == 1) {
                    do {
                        messageNames.add(num.getString(0));
                    } while (num.moveToNext());
                }else{
                    messageNames.add(num.getString(0));
                }

                messageText.add(cursor.getString(2));    //Message
                messageReceived.add(cursor.getString(3));    //Date and Time Received
                messageSent.add(cursor.getString(4));    //Date and Time Sent

            }

        }

    }

    //ON ITEM CLICK FROM RECYCLER VIEW
    @Override
    public void onItemClick(View view, int position) {
        AlertDialog.Builder builder = new AlertDialog.Builder(context);
        View viewInflated = LayoutInflater.from(context).inflate(R.layout.dialog_inboxmessage, (ViewGroup) getActivity().findViewById(android.R.id.content), false);
        dialog_name = (TextView) viewInflated.findViewById(R.id.dialog_mname);
        dialog_num = (TextView) viewInflated.findViewById(R.id.dialog_mnumber);
        dialog_mess = (TextView) viewInflated.findViewById(R.id.dialog_message);
        dialog_date = (TextView) viewInflated.findViewById(R.id.dialog_mdate);
        // Set up the text
        dialog_name.setText(messageNames.get(position));
        dialog_num.setText(messageNum.get(position));
        dialog_mess.setText(messageText.get(position));
        if(messageSent.get(position) == null) dialog_date.setText(messageReceived.get(position));
        else dialog_date.setText(messageSent.get(position));

        // Specify the type of input expected; this, for example, sets the input as a password, and will mask the text
        builder.setView(viewInflated);

        builder.setNeutralButton("Delete", new DialogInterface.OnClickListener() {
            @Override
            public void onClick(DialogInterface dialog, int which) {

                dataBaseHelper.deleteOneMessage(messageID.get(position));
                messageNames.remove(position);
                messageText.remove(position);
                messageNum.remove(position);
                messageReceived.remove(position);
                messageSent.remove(position);
                recyclerView.removeViewAt(position);
                inboxListAdapter.notifyItemRemoved(position);
                inboxListAdapter.notifyItemRangeChanged(position, messageText.size());
            }
        });
        builder.setPositiveButton("Back", new DialogInterface.OnClickListener() {
            @Override
            public void onClick(DialogInterface dialog, int which) {
                dialog.cancel();
            }
        });

        builder.show();
    }

}