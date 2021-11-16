package com.example.emav1;

import android.app.AlertDialog;
import android.content.Context;
import android.content.DialogInterface;
import android.database.Cursor;
import android.hardware.usb.UsbDevice;
import android.hardware.usb.UsbDeviceConnection;
import android.hardware.usb.UsbManager;
import android.media.Image;
import android.os.Bundle;

import androidx.annotation.NonNull;
import androidx.annotation.Nullable;
import androidx.fragment.app.Fragment;
import androidx.recyclerview.widget.LinearLayoutManager;
import androidx.recyclerview.widget.RecyclerView;
import androidx.transition.TransitionInflater;

import android.view.LayoutInflater;
import android.view.View;
import android.view.ViewGroup;
import android.widget.EditText;
import android.widget.ImageButton;
import android.widget.Toast;

import com.example.emav1.toolspack.PacketHandler;
import com.felhr.usbserial.UsbSerialDevice;

import java.util.ArrayList;

public class FragmentContactList extends Fragment implements ContactListAdapter.ItemClickListener{

    public final String ACTION_USB_PERMISSION = "com.example.emav1.USB_PERMISSION";
    ImageButton addContact;
    Toast toast_send;

    private static ArrayList<String> contactNames, contactNum, contactKey, contactID;

    private EditText editName, editNumber, editKey;
    private RecyclerView recyclerView;

    static ContactListAdapter contactListAdapter;
    DataBaseHelper dataBaseHelper;
    PacketHandler packetHandler;

    Context context;

    @Override
    public View onCreateView(LayoutInflater inflater, ViewGroup container,
                             Bundle savedInstanceState) {
        // Inflate the layout for this fragment
        return inflater.inflate(R.layout.fragment_contact_list, container, false);
    }

    @Override
    public void onViewCreated(@NonNull View view, @Nullable @org.jetbrains.annotations.Nullable Bundle savedInstanceState) {
        super.onViewCreated(view, savedInstanceState);

        context = getActivity();
        addContact = (ImageButton) getActivity().findViewById(R.id.contactlist_addcontactbutton);

        packetHandler = new PacketHandler();

        // to be populated with db Data
        contactID = new ArrayList<>();
        contactNames = new ArrayList<>();
        contactNum = new ArrayList<>();
        contactKey = new ArrayList<>();

        dataBaseHelper = new DataBaseHelper(context);
        storeDBtoArrays();

        // set up the RecyclerView
        recyclerView = getActivity().findViewById(R.id.contactList_list);
        recyclerView.setLayoutManager(new LinearLayoutManager(context));
        contactListAdapter = new ContactListAdapter(context, contactNames, contactNum);
        contactListAdapter.setClickListener(this);
        recyclerView.setAdapter(contactListAdapter);

        //Add Contact Listener
        addContact.setOnClickListener(new View.OnClickListener()
        {
            @Override
            public void onClick(View v)
            {
                onClickAddContact(v);
            }
        });

    }


    void storeDBtoArrays(){
        Cursor cursor = dataBaseHelper.readAllDataContactsTable();
        if(cursor.getCount() == 0){
            Toast.makeText(context, "No Contacts Found!", Toast.LENGTH_SHORT).show();
        }else{
            while (cursor.moveToNext()){
                contactID.add(cursor.getString(0));     //ID
                contactNames.add(cursor.getString(1));  //Names
                contactNum.add(cursor.getString(2));    //Number
                contactKey.add(cursor.getString(3));    //Cipher Key
            }
        }
    }

    public void onClickAddContact(View view){
        AlertDialog.Builder builder = new AlertDialog.Builder(context);
        // I'm using fragment here so I'm using getView() to provide ViewGroup
        // but you can provide here any other instance of ViewGroup from your Fragment / Activity
        View viewInflated = LayoutInflater.from(context).inflate(R.layout.dialog_contactmanagement, (ViewGroup) getActivity().findViewById(android.R.id.content), false);
        // Set up the input
        //final EditText input = (EditText) viewInflated.findViewById(R.id.input);
        editName = (EditText) viewInflated.findViewById(R.id.dialog_name);
        editNumber = (EditText) viewInflated.findViewById(R.id.dialog_number);
        editKey = (EditText) viewInflated.findViewById(R.id.dialog_key);
        builder.setView(viewInflated);

        // Set up the buttons
        builder.setPositiveButton("Add", new DialogInterface.OnClickListener() {
            @Override
            public void onClick(DialogInterface dialog, int which) {
                if(editName.getText().toString().equals("") || editNumber.getText().toString().equals("") || editKey.getText().toString().equals("")){
                    Toast.makeText(context, "Please fill up all fields!", Toast.LENGTH_SHORT).show();
                }else{
                    contactNames.add(editName.getText().toString());
                    contactNum.add(editNumber.getText().toString());
                    contactKey.add(editKey.getText().toString());
                    dataBaseHelper.addOneContact(editName.getText().toString().trim(), editNumber.getText().toString(),
                            editKey.getText().toString().trim());

                    //refill the contact Array lists so that the Contact ID will be filled with the new information
                    contactID.clear();
                    contactNames.clear();
                    contactNum.clear();
                    contactKey.clear();
                    storeDBtoArrays();

                    //Redisplay the list
                    contactListAdapter.notifyDataSetChanged();
                }
            }
        });
        builder.setNegativeButton("Cancel", new DialogInterface.OnClickListener() {
            @Override
            public void onClick(DialogInterface dialog, int which) {
                dialog.cancel();
            }
        });

        builder.show();
    }

    @Override
    public void onItemClick(View view, int position) {
        //Toast.makeText(this, "You clicked " + contactListAdapter.getName(position) + " on row number " + position + ". Add Edit and Delete Functions Here", Toast.LENGTH_SHORT).show();

        AlertDialog.Builder builder = new AlertDialog.Builder(context);
        // I'm using fragment here so I'm using getView() to provide ViewGroup
        // but you can provide here any other instance of ViewGroup from your Fragment / Activity
        View viewInflated = LayoutInflater.from(context).inflate(R.layout.dialog_contactmanagement, (ViewGroup) getActivity().findViewById(android.R.id.content), false);
        // Set up the input
        //final EditText input = (EditText) viewInflated.findViewById(R.id.input);
        editName = (EditText) viewInflated.findViewById(R.id.dialog_name);
        editNumber = (EditText) viewInflated.findViewById(R.id.dialog_number);
        editKey = (EditText) viewInflated.findViewById(R.id.dialog_key);
        editName.setText(contactNames.get(position));
        editNumber.setText(contactNum.get(position));
        editKey.setText(contactKey.get(position));
        // Specify the type of input expected; this, for example, sets the input as a password, and will mask the text
        builder.setView(viewInflated);
        // Set up the buttons
            builder.setPositiveButton("Edit", new DialogInterface.OnClickListener() {
                @Override
                public void onClick(DialogInterface dialog, int which) {
                    try {
                        if (editName.getText().toString() == "" || editNumber.getText().toString() == "" || editKey.getText().toString() == "") {
                            toast_send = Toast.makeText(context, "Please fill up all fields!", Toast.LENGTH_SHORT);
                            toast_send.show();
                        } else {
                            dataBaseHelper.updateContact(contactID.get(position), editName.getText().toString(), editNumber.getText().toString(),
                                    editKey.getText().toString());
                            contactNames.set(position, editName.getText().toString());
                            contactNum.set(position, editNumber.getText().toString());
                            contactKey.set(position, editKey.getText().toString());
                            contactListAdapter.notifyDataSetChanged();
                            Toast.makeText(context, "Please Reconnect the EMA device", Toast.LENGTH_SHORT).show();
                        }
                    } catch (Exception e) {
                        toast_send = Toast.makeText(context, "Edit Error", Toast.LENGTH_SHORT);
                        toast_send.show();
                    }
                }
            });

        if(position > 0) {     // Show edit and delete button if not User's Contact Number
            builder.setNeutralButton("Delete", new DialogInterface.OnClickListener() {
                @Override
                public void onClick(DialogInterface dialog, int which) {
                    AlertDialog.Builder builder_son = new AlertDialog.Builder(context);
                    builder_son.setTitle("Delete");
                    builder_son.setMessage("Are you sure you want to delete " + contactListAdapter.getName(position) + "?");
                    builder_son.setPositiveButton("Yes", new DialogInterface.OnClickListener() {
                        @Override
                        public void onClick(DialogInterface dialog, int which) {
                            try {
                                dataBaseHelper.deleteOneContact(contactID.get(position));
                                contactNames.remove(position);
                                contactNum.remove(position);
                                contactListAdapter.notifyDataSetChanged();
                            } catch (Exception e) {
                                Toast.makeText(context, "Delete Error", Toast.LENGTH_SHORT).show();
                            }

                        }

                    });
                    builder_son.setNegativeButton("No", new DialogInterface.OnClickListener() {
                        @Override
                        public void onClick(DialogInterface dialog, int which) {
                            Toast.makeText(context, "Cancelled Delete Function", Toast.LENGTH_SHORT).show();
                        }
                    });

                    builder_son.create().show();
                }
            });
        }

        builder.setNegativeButton("Back", new DialogInterface.OnClickListener() {
            @Override
            public void onClick(DialogInterface dialog, int which) {
                dialog.cancel();
            }
        });

        builder.show();
    }

}