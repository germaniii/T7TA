package com.example.emav1;

import android.app.AlertDialog;
import android.app.PendingIntent;
import android.content.BroadcastReceiver;
import android.content.Context;
import android.content.DialogInterface;
import android.content.Intent;
import android.content.IntentFilter;
import android.database.Cursor;
import android.hardware.usb.UsbDevice;
import android.hardware.usb.UsbDeviceConnection;
import android.hardware.usb.UsbManager;
import android.os.Bundle;
import android.util.Log;
import android.view.LayoutInflater;
import android.view.View;
import android.view.ViewGroup;
import android.widget.EditText;
import android.widget.ImageButton;
import android.widget.TextView;
import android.widget.Toast;

import androidx.appcompat.app.AppCompatActivity;
import androidx.recyclerview.widget.LinearLayoutManager;
import androidx.recyclerview.widget.RecyclerView;

import com.felhr.usbserial.UsbSerialDevice;
import com.felhr.usbserial.UsbSerialInterface;

import java.sql.DatabaseMetaData;
import java.util.ArrayList;
import java.util.HashMap;
import java.util.Map;

public class ContactListActivity extends AppCompatActivity implements ContactListAdapter.ItemClickListener{

    public final String ACTION_USB_PERMISSION = "com.example.emav1.USB_PERMISSION";
    UsbManager usbManager;
    UsbDevice device;
    UsbSerialDevice serialPort;
    UsbDeviceConnection connection;
    ImageButton beacon;
    Toast toast_send;

    private ArrayList<String> contactNames, contactNum, contactKey;

    private EditText editName, editNumber, editKey;
    private RecyclerView recyclerView;

    ContactListAdapter contactListAdapter;
    DataBaseHelper dataBaseHelper;

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_contactlist);

        usbManager = (UsbManager) getSystemService(MainActivity.USB_SERVICE);
        beacon = findViewById(R.id.contactList_beaconButton);

        // data to populate the RecyclerView with
        contactNames = new ArrayList<>();
        contactNum = new ArrayList<>();
        contactKey = new ArrayList<>();

        dataBaseHelper = new DataBaseHelper(ContactListActivity.this);
        storeDBtoArrays();
        // set up the RecyclerView
        recyclerView = findViewById(R.id.contactList_list);
        recyclerView.setLayoutManager(new LinearLayoutManager(this));
        contactListAdapter = new ContactListAdapter(this, contactNames, contactNum);
        contactListAdapter.setClickListener(this);
        recyclerView.setAdapter(contactListAdapter);

        IntentFilter filter = new IntentFilter();
        filter.addAction(ACTION_USB_PERMISSION);
        filter.addAction(UsbManager.ACTION_USB_DEVICE_ATTACHED);
        filter.addAction(UsbManager.ACTION_USB_DEVICE_DETACHED);
        registerReceiver(broadcastReceiver, filter);
        if(!arduinoConnected()){
            //beacon.setImageResource(R.drawable.icon_beacon_on);
        }
    }

    public boolean arduinoConnected() {
        HashMap<String, UsbDevice> usbDevices = usbManager.getDeviceList();
        boolean keep = true;
        if (!usbDevices.isEmpty()) {
            for (Map.Entry<String, UsbDevice> entry : usbDevices.entrySet()) {
                device = entry.getValue();
                int deviceVID = device.getVendorId();
                if (deviceVID == 0x1A86 || deviceVID == 0x2341 || deviceVID == 0x0403)//Arduino UNO and Nano Vendor ID
                {
                    PendingIntent pi = PendingIntent.getBroadcast(this, 0, new Intent(ACTION_USB_PERMISSION), 0);
                    usbManager.requestPermission(device, pi);
                    keep = false;
                } else {
                    connection = null;
                    device = null;
                }

                if (!keep)
                    break;
            }
        }
        return keep;
    }

    public void arduinoDisconnected() {
        serialPort.close();
        toast_send = Toast.makeText(ContactListActivity.this, "Serial Connection Closed!", Toast.LENGTH_SHORT);
        toast_send.show();
    }

    UsbSerialInterface.UsbReadCallback mCallback = new UsbSerialInterface.UsbReadCallback() { //Defining a Callback which triggers whenever data is read.
        @Override
        public void onReceivedData(byte[] arg0) {
            String data;
           /* try {
                //This will be using a background method for processing received packets
            } catch (UnsupportedEncodingException e) {
                e.printStackTrace();
            }*/
        }
    };

    private final BroadcastReceiver broadcastReceiver = new BroadcastReceiver() { //Broadcast Receiver to automatically start and stop the Serial connection.
        @Override
        public void onReceive(Context context, Intent intent) {
            if (intent.getAction().equals(ACTION_USB_PERMISSION)) {
                boolean granted = intent.getExtras().getBoolean(UsbManager.EXTRA_PERMISSION_GRANTED);
                if (granted) {
                    connection = usbManager.openDevice(device);
                    serialPort = UsbSerialDevice.createUsbSerialDevice(device, connection);
                    if (serialPort != null) {
                        if (serialPort.open()) { //Set Serial Connection Parameters.
                            serialPort.setBaudRate(9600);
                            serialPort.setDataBits(UsbSerialInterface.DATA_BITS_8);
                            serialPort.setStopBits(UsbSerialInterface.STOP_BITS_1);
                            serialPort.setParity(UsbSerialInterface.PARITY_NONE);
                            serialPort.setFlowControl(UsbSerialInterface.FLOW_CONTROL_OFF);
                            serialPort.read(mCallback);
                            toast_send = Toast.makeText(ContactListActivity.this, "Serial Connection Opened!", Toast.LENGTH_SHORT);
                            toast_send.show();

                        }else{
                            Log.d("SERIAL", "PORT NOT OPEN");
                        }
                    }else{
                        Log.d("SERIAL", "PORT IS NULL");
                    }
                }else{
                    Log.d("SERIAL", "PERM NOT GRANTED");
                }
            } else if (intent.getAction().equals(UsbManager.ACTION_USB_DEVICE_ATTACHED)) {
                arduinoConnected();
                //beacon.setImageResource(R.drawable.icon_beacon_on);
                //sendButton.setImageResource(R.drawable.icon_textmode_on);
                toast_send = Toast.makeText(ContactListActivity.this, "EMA device connected!", Toast.LENGTH_SHORT);
                toast_send.show();
            } else if (intent.getAction().equals(UsbManager.ACTION_USB_DEVICE_DETACHED)) {
                arduinoDisconnected();
                //beacon.setImageResource(R.drawable.icon_beacon_off);
                //sendButton.setImageResource(R.drawable.icon_textmode_off);
                toast_send = Toast.makeText(ContactListActivity.this, "EMA device disconnected!", Toast.LENGTH_SHORT);
                toast_send.show();
            }
        }
    };

    public void onClickBeaconMode(View view){
        try{
            if(beacon.isEnabled()){
                for(int i=5; i>0; i--) {
                   // Transmit Beacon Mode Function
                }
            }
        }catch(Exception e){
            toast_send = Toast.makeText(ContactListActivity.this, "Please Connect the EMA Device!", Toast.LENGTH_SHORT);
            toast_send.show();
        }

    }

    public void onClickAddContact(View view){
        AlertDialog.Builder builder = new AlertDialog.Builder(ContactListActivity.this);
        // I'm using fragment here so I'm using getView() to provide ViewGroup
        // but you can provide here any other instance of ViewGroup from your Fragment / Activity
        View viewInflated = LayoutInflater.from(this).inflate(R.layout.dialog_contactmanagement, (ViewGroup) findViewById(android.R.id.content), false);
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
                if(editName.getText().toString() == "" || editNumber.getText().toString() == "" || editKey.getText().toString() == ""){
                    toast_send = Toast.makeText(ContactListActivity.this, "Please fill up all fields!", Toast.LENGTH_SHORT);
                    toast_send.show();
                }else{
                    contactNames.add(editName.getText().toString());
                    contactNum.add(editNumber.getText().toString());
                    contactKey.add(editKey.getText().toString());
                    dataBaseHelper.addOneContact(editName.getText().toString().trim(), editNumber.getText().toString(),
                                            editKey.getText().toString().trim());
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

        AlertDialog.Builder builder = new AlertDialog.Builder(ContactListActivity.this);
        // I'm using fragment here so I'm using getView() to provide ViewGroup
        // but you can provide here any other instance of ViewGroup from your Fragment / Activity
        View viewInflated = LayoutInflater.from(this).inflate(R.layout.dialog_contactmanagement, (ViewGroup) findViewById(android.R.id.content), false);
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
                try{
                    if(editName.getText().toString() == "" || editNumber.getText().toString() == "" || editKey.getText().toString() == ""){
                        toast_send = Toast.makeText(ContactListActivity.this, "Please fill up all fields!", Toast.LENGTH_SHORT);
                        toast_send.show();
                    }else{
                        dataBaseHelper.updateContact(Integer.toString(position+1), editName.getText().toString(), editNumber.getText().toString(),
                                editKey.getText().toString());
                        contactNames.set(position, editName.getText().toString());
                        contactNum.set(position, editNumber.getText().toString());
                        contactKey.set(position, editKey.getText().toString());
                        contactListAdapter.notifyDataSetChanged();
                    }
                }catch(Exception e){
                    toast_send = Toast.makeText(ContactListActivity.this, "Unknown Error", Toast.LENGTH_SHORT);
                    toast_send.show();
                }
            }
        });
        builder.setNeutralButton("Delete", new DialogInterface.OnClickListener() {
            @Override
            public void onClick(DialogInterface dialog, int which) {
                try{
                    AlertDialog.Builder builder_son = new AlertDialog.Builder(ContactListActivity.this);
                    builder_son.setTitle("Delete");
                    builder_son.setMessage("Are you sure you want to delete " + contactListAdapter.getName(position) + "?");
                    builder_son.setPositiveButton("Yes", new DialogInterface.OnClickListener() {
                        @Override
                        public void onClick(DialogInterface dialog, int which) {
                            dataBaseHelper.deleteOneContact(Integer.toString(position+1));
                            contactNames.remove(position);
                            contactNum.remove(position);
                            recyclerView.removeViewAt(position);
                            contactListAdapter.notifyItemRemoved(position);
                            contactListAdapter.notifyItemRangeChanged(position, contactNames.size());
                        }
                    });
                    builder_son.setNegativeButton("No", new DialogInterface.OnClickListener() {
                        @Override
                        public void onClick(DialogInterface dialog, int which) {
                            Toast.makeText(ContactListActivity.this, "Cancelled Delete Function", Toast.LENGTH_SHORT).show();
                        }
                    });

                    builder_son.create().show();

                }catch(Exception e){
                    Toast.makeText(ContactListActivity.this, "Unknown Error", Toast.LENGTH_SHORT).show();
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

    void storeDBtoArrays(){
        Cursor cursor = dataBaseHelper.readAllData();
        if(cursor.getCount() == 0){
            Toast.makeText(this, "No Contacts Found!", Toast.LENGTH_SHORT).show();
        }else{
            while (cursor.moveToNext()){
                contactNames.add(cursor.getString(1));
                contactNum.add(cursor.getString(2));
                contactKey.add(cursor.getString(3));
            }
        }
    }

    public void onBackPressed() {
        //Go back to Main Activity
        this.finish();
    }

}
