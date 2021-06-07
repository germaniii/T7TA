package com.example.emav1;

import androidx.appcompat.app.AppCompatActivity;
import androidx.appcompat.app.AppCompatDelegate;
import androidx.recyclerview.widget.LinearLayoutManager;
import androidx.recyclerview.widget.RecyclerView;

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
import android.text.method.ScrollingMovementMethod;
import android.util.Log;
import android.view.LayoutInflater;
import android.view.View;
import android.view.ViewGroup;
import android.widget.Button;
import android.widget.EditText;
import android.widget.ImageButton;
import android.widget.TextView;
import android.widget.Toast;

import com.felhr.usbserial.UsbSerialDevice;
import com.felhr.usbserial.UsbSerialInterface;

import org.w3c.dom.Text;

import java.io.UnsupportedEncodingException;
import java.util.ArrayList;
import java.util.HashMap;
import java.util.Map;

public class MainActivity extends AppCompatActivity implements InboxListAdapter.ItemClickListener{

    public final String ACTION_USB_PERMISSION = "com.example.emav1.USB_PERMISSION";
    Button sendButton;
    TextView textView;
    EditText editText;
    UsbManager usbManager;
    UsbDevice device;
    UsbSerialDevice serialPort;
    UsbDeviceConnection connection;
    ImageButton  beacon, toTextMode, toContactList;
    Toast toast_send;

    InboxListAdapter inboxListAdapter;
    ArrayList<String> contactNames, contactNum, contactMessage;
    RecyclerView recyclerView;
    TextView dialog_name, dialog_num, dialog_mess;
    EditText uName, uNumber;

    DataBaseHelper dataBaseHelper;

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        AppCompatDelegate.setDefaultNightMode(AppCompatDelegate.MODE_NIGHT_NO);     // Only use light mode
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_main);
        usbManager = (UsbManager) getSystemService(this.USB_SERVICE);
        sendButton = findViewById(R.id.main_buttonSend);
        textView = findViewById(R.id.main_serialMonitor);
        beacon = findViewById(R.id.main_beaconButton);
        toTextMode = findViewById(R.id.toTextModeButton);
        toContactList = findViewById(R.id.toContactList);
        textView.setMovementMethod(new ScrollingMovementMethod());

        dataBaseHelper = new DataBaseHelper(MainActivity.this);
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

        contactMessage = new ArrayList<>();
        contactMessage.add("Lorem Ipsum Dolor Sit Amet Lorem Ipsum Dolor Sit AmetLorem Ipsum Dolor Sit Amet");
        contactMessage.add("Amet Ipsum Ipsum Ipsum IpsumLorem Ipsum Dolor Sit AmetLorem Ipsum Dolor Sit AmetLorem Ipsum Dolor Sit AmetLorem Ipsum Dolor Sit Amet ");
        contactMessage.add("Amet Amet Amet Amet Amet AmetLorem Ipsum Dolor Sit AmetLorem Ipsum Dolor Sit AmetLorem Ipsum Dolor Sit Amet");
        contactMessage.add("Dolor DolorDolor Dolor DolorLorem Ipsum Dolor Sit AmetLorem Ipsum Dolor Sit AmetLorem Ipsum Dolor Sit AmetLorem Ipsum Dolor Sit Amet");
        contactMessage.add("Lorem Lorem Lorem Lorem LoremLorem Ipsum Dolor Sit AmetLorem Ipsum Dolor Sit AmetLorem Ipsum Dolor Sit Amet");
        contactMessage.add("Ipsum Ipsum Ipsum IpsumIpsumLorem Ipsum Dolor Sit AmetLorem Ipsum Dolor Sit AmetLorem Ipsum Dolor Sit Amet");
        contactMessage.add("Sit Sit   Sit Sit Sit SitLorem Ipsum Dolor Sit AmetLorem Ipsum Dolor Sit AmetLorem Ipsum Dolor Sit Amet");
        contactMessage.add("Ipsum Ipsum Sit SitLorem Ipsum Dolor Sit AmetLorem Ipsum Dolor Sit AmetLorem Ipsum Dolor Sit Amet");
        contactMessage.add("Sit Sit  IpsumIpsumIpsumIpsumIpsumIpsumLorem Ipsum Dolor Sit AmetLorem Ipsum Dolor Sit AmetLorem Ipsum Dolor Sit Amet");
        contactMessage.add("DolorDolor DolorDolor DolorDolorDolorDolorLorem Ipsum Dolor Sit AmetLorem Ipsum Dolor Sit AmetLorem Ipsum Dolor Sit AmetLorem Ipsum Dolor Sit Amet");

        // set up the RecyclerView
        recyclerView = findViewById(R.id.main_inboxList);
        recyclerView.setLayoutManager(new LinearLayoutManager(this));
        inboxListAdapter = new InboxListAdapter(this, contactNames, contactNum, contactMessage);
        inboxListAdapter.setClickListener(this);
        recyclerView.setAdapter(inboxListAdapter);

        IntentFilter filter = new IntentFilter();
        filter.addAction(ACTION_USB_PERMISSION);
        filter.addAction(UsbManager.ACTION_USB_DEVICE_ATTACHED);
        filter.addAction(UsbManager.ACTION_USB_DEVICE_DETACHED);
        registerReceiver(broadcastReceiver, filter);

        // Set Beacon Image Whenever Transmission Device is Connected
        if(!arduinoConnected()){
            //beacon.setImageResource(R.drawable.icon_beacon_on);
        }

        //Checks if Contacts is Empty, if yes, will ask for user's contact number(last 4 digits)
        if(!dataBaseHelper.readAllDataContactsTable().moveToFirst()){
            AlertDialog.Builder builder = new AlertDialog.Builder(MainActivity.this);
            builder.setTitle("User Set-up");
            builder.setMessage("\nPlease input your name and the last 4 digits of your phone number.");
            // I'm using fragment here so I'm using getView() to provide ViewGroup
            // but you can provide here any other instance of ViewGroup from your Fragment / Activity
            View viewInflated = LayoutInflater.from(this).inflate(R.layout.dialog_usercontact, (ViewGroup) findViewById(android.R.id.content), false);
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
                        toast_send = Toast.makeText(MainActivity.this, "Please fill up all fields!", Toast.LENGTH_SHORT);
                        toast_send.show();
                    }else{
                        String uNameString = uName.getText().toString().trim();
                        uNameString += " (My Number)";
                        dataBaseHelper.addOneContact(uNameString, uNumber.getText().toString(), "");
                    }
                }
            });

            builder.show();
        }
        //if(contacts database is empty){
        //alertDialog
        // Ask for last 4 digits of the user's phone number
        // if (contactsDatabase.readAllData() == null)
        // Store to Contacts Database
        // contactNames.add(editName.getText().toString());
        //                    contactNum.add(editNumber.getText().toString());
        //                    contactKey.add(editKey.getText().toString());
        //                    dataBaseHelper.addOneContact(editName.getText().toString().trim(), editNumber.getText().toString(),
        //                                            editKey.getText().toString().trim());
        // }

    }

    UsbSerialInterface.UsbReadCallback mCallback = new UsbSerialInterface.UsbReadCallback() { //Defining a Callback which triggers whenever data is read.
        @Override
        public void onReceivedData(byte[] arg0) {
            String data;
            try {
                data = new String(arg0, "UTF-8");
                //if(data == 'noSID'){  <--- if arduino sends a no senderID message, ask for it
                // serialPort.write(last 4 numbers of the phone number);
                //
                //
                // }
                data.concat("/n");
                tvAppend(textView, data);
            } catch (UnsupportedEncodingException e) {
                e.printStackTrace();
            }
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
                            //

                            toast_send = Toast.makeText(MainActivity.this, "Serial Connection Opened!", Toast.LENGTH_SHORT);
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
                //toTextMode.setImageResource(R.drawable.icon_textmode_on);
                toast_send = Toast.makeText(MainActivity.this, "EMA device connected!", Toast.LENGTH_SHORT);
                toast_send.show();
            } else if (intent.getAction().equals(UsbManager.ACTION_USB_DEVICE_DETACHED)) {
                arduinoDisconnected();
                //beacon.setImageResource(R.drawable.icon_beacon_off);
                //toTextMode.setImageResource(R.drawable.icon_textmode_off);
                toast_send = Toast.makeText(MainActivity.this, "EMA device disconnected!", Toast.LENGTH_SHORT);
                toast_send.show();
            }
        }
    };

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
        toast_send = Toast.makeText(MainActivity.this, "Serial Connection Closed!", Toast.LENGTH_SHORT);
        toast_send.show();
    }

    public void onClickSendTest(View view) {
        try {
            // THIS CAN BE USED FOR SIMULATION OF RECEIVING PACKETS
            String string = editText.getText().toString();
            serialPort.write(string.getBytes());
            tvAppend(textView, "\nData Sent : " + string + "\n");
        }catch (Exception e){
            toast_send = Toast.makeText(MainActivity.this, "Send and Receive Test Error", Toast.LENGTH_SHORT);
            toast_send.show();
        }
    }

    public void onClickBeaconMode(View view){
        try{
            if(beacon.isEnabled())
            for(int i=5; i>0; i--) {
                String string = "help";
                serialPort.write(string.getBytes());
                tvAppend(textView, "\nINFO:\n" + string + "\n");
            }
        }catch(Exception e){
            toast_send = Toast.makeText(MainActivity.this, "Please Connect the EMA Device!", Toast.LENGTH_SHORT);
            toast_send.show();
        }

    }

    public void onClicktoTextMode(View view) {
            // Go to Text Message Mode
            Intent intent = new Intent(this, TextMessageActivity.class);
            MainActivity.this.startActivity(intent);
    }

    public void onClicktoContactList(View view) {
        // Go to Contact List
        Intent intent = new Intent(this, ContactListActivity.class);
        MainActivity.this.startActivity(intent);
    }

    //ON ITEM CLICK FROM RECYCLER VIEW
    @Override
    public void onItemClick(View view, int position) {
        Toast.makeText(this, "You clicked " + inboxListAdapter.getName(position) + " on row number " + position + ". Add Edit and Delete Functions Here", Toast.LENGTH_SHORT).show();
        AlertDialog.Builder builder = new AlertDialog.Builder(MainActivity.this);
        View viewInflated = LayoutInflater.from(this).inflate(R.layout.dialog_inboxmessage, (ViewGroup) findViewById(android.R.id.content), false);
        dialog_name = (TextView) viewInflated.findViewById(R.id.dialog_mname);
        dialog_num = (TextView) viewInflated.findViewById(R.id.dialog_mnumber);
        dialog_mess = (TextView) viewInflated.findViewById(R.id.dialog_message);
        // Set up the text
        dialog_name.setText(contactNames.get(position));
        dialog_num.setText(contactNum.get(position));
        dialog_mess.setText(contactMessage.get(position));
        // Specify the type of input expected; this, for example, sets the input as a password, and will mask the text
        builder.setView(viewInflated);

        builder.setNeutralButton("Delete", new DialogInterface.OnClickListener() {
            @Override
            public void onClick(DialogInterface dialog, int which) {
                contactNames.remove(position);
                contactNum.remove(position);
                recyclerView.removeViewAt(position);
                inboxListAdapter.notifyItemRemoved(position);
                inboxListAdapter.notifyItemRangeChanged(position, contactNames.size());
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

    void storeDBtoArrays(){
        Cursor cursor = dataBaseHelper.readAllDataContactsTable();
        if(cursor.getCount() == 0){
            Toast.makeText(this, "No Data", Toast.LENGTH_SHORT).show();
        }else{
            while (cursor.moveToNext()){
                contactNames.add(cursor.getString(0));
                contactNum.add(cursor.getString(1));
                contactMessage.add(cursor.getString(2));
            }
        }
    }

    public void onBackPressed() {
        //doing nothing on pressing Back key
    }
}