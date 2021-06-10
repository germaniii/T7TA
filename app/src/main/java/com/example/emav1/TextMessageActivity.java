package com.example.emav1;

import android.app.PendingIntent;
import android.content.BroadcastReceiver;
import android.content.Context;
import android.content.Intent;
import android.content.IntentFilter;
import android.database.Cursor;
import android.hardware.usb.UsbDevice;
import android.hardware.usb.UsbDeviceConnection;
import android.hardware.usb.UsbManager;
import android.os.Bundle;
import android.text.method.ScrollingMovementMethod;
import android.util.Log;
import android.view.View;
import android.widget.ArrayAdapter;
import android.widget.EditText;
import android.widget.ImageButton;
import android.widget.Spinner;
import android.widget.TextView;
import android.widget.Toast;

import androidx.appcompat.app.AppCompatActivity;

import com.felhr.usbserial.UsbSerialDevice;
import com.felhr.usbserial.UsbSerialInterface;

import org.w3c.dom.Text;

import java.io.UnsupportedEncodingException;
import java.sql.DatabaseMetaData;
import java.util.ArrayList;
import java.util.Arrays;
import java.util.HashMap;
import java.util.Map;

public class TextMessageActivity extends AppCompatActivity {

    public final String ACTION_USB_PERMISSION = "com.example.emav1.USB_PERMISSION";
    TextView textView;
    UsbManager usbManager;
    UsbDevice device;
    UsbSerialDevice serialPort;
    UsbDeviceConnection connection;
    ImageButton  beacon, sendButton;
    EditText message;
    Spinner number;
    ArrayList<String> spinnerContacts;
    DataBaseHelper dataBaseHelper;
    PacketHandler packetHandler;

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_textmessage);

        usbManager = (UsbManager) getSystemService(MainActivity.USB_SERVICE);
        beacon = findViewById(R.id.textMessage_beaconButton);
        textView = findViewById(R.id.textMessage_serialMonitor);
        sendButton = findViewById(R.id.textMessage_sendButton);
        message = findViewById(R.id.textMessage_message);

        packetHandler = new PacketHandler();
        dataBaseHelper = new DataBaseHelper(TextMessageActivity.this);
        spinnerContacts = new ArrayList<>();
        storeDBtoArrays();


        number = findViewById(R.id.textMessage_Spinner);
        ArrayAdapter<String> adapter = new ArrayAdapter<>(this,
                android.R.layout.simple_spinner_item, spinnerContacts);
        adapter.setDropDownViewResource(android.R.layout.simple_spinner_dropdown_item);
        number.setAdapter(adapter);

        textView.setMovementMethod(new ScrollingMovementMethod());
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
        sendButton.setEnabled(false);
    }

    UsbSerialInterface.UsbReadCallback mCallback = new UsbSerialInterface.UsbReadCallback() { //Defining a Callback which triggers whenever data is read.
        @Override
        public void onReceivedData(byte[] arg0) {
            String data;
            byte[] stream;
            try {
                stream = arg0;
                data = new String(arg0, "UTF-8");

                // Control Code 1, Send SID to Arduino Device
                if(stream[0] == 1) {
                    tvAppend(textView, "Received " + stream[0] + "\n");
                    serialPort.write(getUserSID().getBytes());
                    sendButton.setEnabled(false);
                    tvAppend(textView, "OutStream : " + getUserSID() + "\n");
                }else if(stream[0] == 2){
                    // Control Code 2, Send from MessagesOut_Table, from TextMessagingMode
                }else if(stream[0] == 3){
                    // Control Code 3, Receive Messages and store to MessagesIn_Table
                }

                tvAppend(textView, "InStream : " + data);
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
                Toast.makeText(TextMessageActivity.this, "EMA Device Connected", Toast.LENGTH_SHORT).show();
            } else if (intent.getAction().equals(UsbManager.ACTION_USB_DEVICE_DETACHED)) {
                arduinoDisconnected();
                //beacon.setImageResource(R.drawable.icon_beacon_off);
                //sendButton.setImageResource(R.drawable.icon_textmode_off);
                Toast.makeText(TextMessageActivity.this, "EMA Device Disconnected", Toast.LENGTH_SHORT).show();
            }
        }
    };

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

    public void onClickSendButton(View view) {
        try {
            if (sendButton.isEnabled()){
                if ((message.getText().length() == 0) || number.getSelectedItem() == "") {
                    Toast.makeText(TextMessageActivity.this, "Please Fill Up All Fields!", Toast.LENGTH_SHORT).show();
                }else {
                    String string = message.getText().toString();
                    serialPort.write(string.getBytes());
                    tvAppend(textView, "\nTransmit: " + string  + " to " + number.getSelectedItem() + "\n");
                }
            }else
                Toast.makeText(TextMessageActivity.this, "Synchronizing EMA Device, Please Wait", Toast.LENGTH_SHORT).show();
        }catch (Exception e){
            Toast.makeText(TextMessageActivity.this, "Please Connect EMA Device", Toast.LENGTH_SHORT).show();
        }
    }

    public void onClickBeaconMode(View view){
        try{
            if(beacon.isEnabled()){
                for(int i=5; i>0; i--) {
                    String string = "help";
                    serialPort.write(string.getBytes());
                    tvAppend(textView, "\nINFO:\n" + string + "\n");
                }
            }
        }catch(Exception e){
            Toast.makeText(TextMessageActivity.this, "Please Connect the EMA Device!", Toast.LENGTH_SHORT).show();
        }

    }

    void storeDBtoArrays(){
        Cursor cursor = dataBaseHelper.readAllDataContactsTable();
        if(cursor.getCount() <= 1){
            Toast.makeText(this, "No Contacts Found!", Toast.LENGTH_SHORT).show();
        }else{
            if(cursor.moveToFirst()){
                while (cursor.moveToNext()){
                    spinnerContacts.add(cursor.getString(1));  //Names
                }
            }
        }
    }

    String getUserSID(){
        String SID = null;
        Cursor cursor;
        cursor = dataBaseHelper.readUserSID();
        if(cursor.getCount() == 0){
            Toast.makeText(TextMessageActivity.this, "No User SID!", Toast.LENGTH_SHORT).show();
        }else{
            while (cursor.moveToNext())
                SID = cursor.getString(0);     //CONTACT NUM
        }
        return SID;
    }

    @Override protected void onDestroy() { super.onDestroy(); unregisterReceiver(broadcastReceiver); }

    public void onBackPressed() {
        //Go back to Main Activity
        this.finish();
    }

}
