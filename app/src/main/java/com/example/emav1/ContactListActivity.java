package com.example.emav1;

import android.app.AlertDialog;
import android.app.PendingIntent;
import android.content.BroadcastReceiver;
import android.content.Context;
import android.content.DialogInterface;
import android.content.Intent;
import android.content.IntentFilter;
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

    private ArrayList<String> contactNames, contactNum;

    private EditText editName, editNumber;
    private RecyclerView recyclerView;

    ContactListAdapter contactListAdapter;

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_contactlist);

        usbManager = (UsbManager) getSystemService(MainActivity.USB_SERVICE);
        beacon = findViewById(R.id.contactList_beaconButton);

        // data to populate the RecyclerView with
        contactNames = new ArrayList<>();
        contactNames.add("German");
        contactNames.add("Carlo");
        contactNames.add("Adrian");
        contactNames.add("Francis");
        contactNames.add("Sir Obette");
        contactNames.add("Jabo");
        contactNames.add("Jess");
        contactNames.add("Eli");
        contactNames.add("Ellaine");
        contactNames.add("Kier");

        contactNum = new ArrayList<>();
        contactNum.add("09159301068");
        contactNum.add("09919301677");
        contactNum.add("09123901128");
        contactNum.add("09159301129");
        contactNum.add("09559301005");
        contactNum.add("09669301583");
        contactNum.add("09167930181");
        contactNum.add("09189830167");
        contactNum.add("09158630123");
        contactNum.add("09134930102");

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
        builder.setView(viewInflated);

        // Set up the buttons
        builder.setPositiveButton("Add", new DialogInterface.OnClickListener() {
            @Override
            public void onClick(DialogInterface dialog, int which) {
                if(editName.getText().toString() == "" || editNumber.getText().toString() == ""){
                    toast_send = Toast.makeText(ContactListActivity.this, "Please fill up all fields!", Toast.LENGTH_SHORT);
                    toast_send.show();
                }else{
                    contactNames.add(editName.getText().toString());
                    contactNum.add(editNumber.getText().toString());
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
        Toast.makeText(this, "You clicked " + contactListAdapter.getName(position) + " on row number " + position + ". Add Edit and Delete Functions Here", Toast.LENGTH_SHORT).show();

        AlertDialog.Builder builder = new AlertDialog.Builder(ContactListActivity.this);
        // I'm using fragment here so I'm using getView() to provide ViewGroup
        // but you can provide here any other instance of ViewGroup from your Fragment / Activity
        View viewInflated = LayoutInflater.from(this).inflate(R.layout.dialog_contactmanagement, (ViewGroup) findViewById(android.R.id.content), false);
        // Set up the input
        //final EditText input = (EditText) viewInflated.findViewById(R.id.input);
        editName = (EditText) viewInflated.findViewById(R.id.dialog_name);
        editNumber = (EditText) viewInflated.findViewById(R.id.dialog_number);

        editName.setText(contactNames.get(position));
        editNumber.setText(contactNum.get(position));
        // Specify the type of input expected; this, for example, sets the input as a password, and will mask the text
        builder.setView(viewInflated);

        // Set up the buttons
        builder.setPositiveButton("Edit", new DialogInterface.OnClickListener() {
            @Override
            public void onClick(DialogInterface dialog, int which) {
                try{
                    contactNames.set(position, editName.getText().toString());
                    contactNum.set(position, editNumber.getText().toString());
                    contactListAdapter.notifyDataSetChanged();
                }catch(Exception e){
                    toast_send = Toast.makeText(ContactListActivity.this, "Please fill up all fields!", Toast.LENGTH_SHORT);
                    toast_send.show();
                }
            }
        });
        builder.setNeutralButton("Delete", new DialogInterface.OnClickListener() {
            @Override
            public void onClick(DialogInterface dialog, int which) {
                contactNames.remove(position);
                contactNum.remove(position);
                recyclerView.removeViewAt(position);
                contactListAdapter.notifyItemRemoved(position);
                contactListAdapter.notifyItemRangeChanged(position, contactNames.size());
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


    private void tvAppend(TextView tv, CharSequence text) {
        final TextView ftv = tv;
        final CharSequence ftext = text;

        runOnUiThread(new Runnable() {
            @Override
            public void run() {
                ftv.append(ftext);
            }
        });
    }

    public void onBackPressed() {
        //Go back to Main Activity
        this.finish();
    }

}
