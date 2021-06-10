package com.example.emav1;

import androidx.annotation.RequiresApi;
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
import android.icu.text.SimpleDateFormat;
import android.icu.util.Calendar;
import android.os.Build;
import android.os.Bundle;
import android.text.format.DateFormat;
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
import java.util.Arrays;
import java.util.Date;
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
    Date date;
    SimpleDateFormat dateFormat;

    PacketHandler packetHandler;
    InboxListAdapter inboxListAdapter;
    ArrayList<String> contactNames, contactNum, contactMessage;
    ArrayList<String> messageID, messageNames, messageNum, messageText, messageReceived, messageSent;
    RecyclerView recyclerView;
    TextView dialog_name, dialog_num, dialog_mess, dialog_date;
    EditText uName, uNumber;

    DataBaseHelper dataBaseHelper;

    @RequiresApi(api = Build.VERSION_CODES.N)
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
        date = Calendar.getInstance().getTime();
        dateFormat = new SimpleDateFormat("hh:mm mm-dd-yyyy");
        packetHandler = new PacketHandler();


        // data to populate the RecyclerView with
        messageID = new ArrayList<>();
        messageNames = new ArrayList<>();
        messageNum = new ArrayList<>();
        messageText = new ArrayList<>();
        messageSent = new ArrayList<>();
        messageReceived = new ArrayList<>();

        dataBaseHelper = new DataBaseHelper(MainActivity.this);
        storeDBtoArrays();

        // set up the RecyclerView
        recyclerView = findViewById(R.id.main_inboxList);
        recyclerView.setLayoutManager(new LinearLayoutManager(this));
        inboxListAdapter = new InboxListAdapter(this, messageNames, messageNum, messageText, messageReceived, messageSent);
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
                        String strDate = dateFormat.format(date);
                        String uNameString = uName.getText().toString().trim();
                        uNameString += " (My Number)";
                        dataBaseHelper.addOneContact(uNameString, uNumber.getText().toString(), "");
                        dataBaseHelper.addOneMessage(uNumber.getText().toString(), "This is a test Message!", strDate, null);

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

    UsbSerialInterface.UsbReadCallback mCallback = new UsbSerialInterface.UsbReadCallback() { //Defining a Callback which triggers whenever data is read.
        @Override
        public void onReceivedData(byte[] arg0) {
            String data;
            try {
                byte[] stream;
                stream = arg0;
                data = new String(arg0, "UTF-8");
                //if(data == 'noSID'){  <--- if arduino sends a no senderID message, ask for it
                // serialPort.write(last 4 numbers of the phone number);
                //
                //
                // }
                tvAppend(textView, data);
                tvAppend(textView, Arrays.toString(stream) + "\n");
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
                toast_send = Toast.makeText(MainActivity.this, "EMA device connected!", Toast.LENGTH_SHORT);
                toast_send.show();
            } else if (intent.getAction().equals(UsbManager.ACTION_USB_DEVICE_DETACHED)) {
                arduinoDisconnected();
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
            String string = "";
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

                dataBaseHelper.deleteOneContact(messageID.get(position));
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
        Cursor cursor = dataBaseHelper.readAllDataMessagesTable();
        Cursor num;
        if(cursor.getCount() == 0){
            Toast.makeText(this, "No Messages Found!", Toast.LENGTH_SHORT).show();
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
                }

                messageText.add(cursor.getString(2));    //Message
                messageReceived.add(cursor.getString(3));    //Date and Time Received
                messageSent.add(cursor.getString(4));    //Date and Time Sent

            }
        }
    }


    @Override protected void onDestroy() { super.onDestroy(); unregisterReceiver(broadcastReceiver); }

    public void onBackPressed() {
        //doing nothing on pressing Back key
    }
}