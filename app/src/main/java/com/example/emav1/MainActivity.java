package com.example.emav1;

import androidx.appcompat.app.AppCompatActivity;

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
import android.view.MotionEvent;
import android.view.View;
import android.view.ViewGroup;
import android.widget.Button;
import android.widget.EditText;
import android.widget.ImageButton;
import android.widget.TextView;
import android.widget.Toast;

import com.felhr.usbserial.UsbSerialDevice;
import com.felhr.usbserial.UsbSerialInterface;

import java.io.UnsupportedEncodingException;
import java.util.HashMap;
import java.util.Map;

public class MainActivity extends AppCompatActivity {

    public final String ACTION_USB_PERMISSION = "com.example.emav1.USB_PERMISSION";
    Button sendButton;
    TextView textView;
    EditText editText, sendMessage, send_username, send_userid;
    UsbManager usbManager;
    UsbDevice device;
    UsbSerialDevice serialPort;
    UsbDeviceConnection connection;
    ImageButton  beacon, toTextMode;
    Toast toast_send;

    UsbSerialInterface.UsbReadCallback mCallback = new UsbSerialInterface.UsbReadCallback() { //Defining a Callback which triggers whenever data is read.
        @Override
        public void onReceivedData(byte[] arg0) {
            String data = null;
            try {
                data = new String(arg0, "UTF-8");
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
                            setUiEnabled(true);
                            serialPort.setBaudRate(9600);
                            serialPort.setDataBits(UsbSerialInterface.DATA_BITS_8);
                            serialPort.setStopBits(UsbSerialInterface.STOP_BITS_1);
                            serialPort.setParity(UsbSerialInterface.PARITY_NONE);
                            serialPort.setFlowControl(UsbSerialInterface.FLOW_CONTROL_OFF);
                            serialPort.read(mCallback);
                            tvAppend(textView,"Serial Connection Opened!\n");

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
                beacon.setImageResource(R.drawable.icon_beacon_on);
                toTextMode.setImageResource(R.drawable.icon_textmode_on);
                toast_send = Toast.makeText(MainActivity.this, "EMA device connected!", Toast.LENGTH_SHORT);
                toast_send.show();
            } else if (intent.getAction().equals(UsbManager.ACTION_USB_DEVICE_DETACHED)) {
                arduinoDisconnected();
                beacon.setImageResource(R.drawable.icon_beacon_off);
                toTextMode.setImageResource(R.drawable.icon_textmode_off);
                toast_send = Toast.makeText(MainActivity.this, "EMA device disconnected!", Toast.LENGTH_SHORT);
                toast_send.show();
            }
        }
    };

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_main);
        usbManager = (UsbManager) getSystemService(this.USB_SERVICE);
        sendButton = (Button) findViewById(R.id.main_buttonSend);
        editText = (EditText) findViewById(R.id.main_toSenderName);
        textView = (TextView) findViewById(R.id.main_serialMonitor);
        beacon = (ImageButton) findViewById(R.id.main_beaconButton);
        toTextMode = (ImageButton) findViewById(R.id.toTextModeButton);
        setUiEnabled(false);
        IntentFilter filter = new IntentFilter();
        filter.addAction(ACTION_USB_PERMISSION);
        filter.addAction(UsbManager.ACTION_USB_DEVICE_ATTACHED);
        filter.addAction(UsbManager.ACTION_USB_DEVICE_DETACHED);
        registerReceiver(broadcastReceiver, filter);
        if(arduinoConnected() == false){
            beacon.setImageResource(R.drawable.icon_beacon_on);
        }

    }

    public void setUiEnabled(boolean bool) {
        sendButton.setEnabled(bool);
        toTextMode.setEnabled(bool);
        beacon.setEnabled(bool);
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
        setUiEnabled(false);
        serialPort.close();
        tvAppend(textView,"\nSerial Connection Closed! \n");
    }

    public void onClickSendTest(View view) {
        String string = editText.getText().toString();
        serialPort.write(string.getBytes());
        tvAppend(textView, "\nData Sent : " + string + "\n");
    }

    public void onClickBeaconMode(View view){
        if(beacon.isEnabled())
        for(int i=5; i>0; i--) {
            String string = "help";
            serialPort.write(string.getBytes());
            tvAppend(textView, "\nINFO:\n" + string + "\n");
        }else{
            // do nothing
        }
    }

    public void onClicktoTextMode(View view) {
        if(toTextMode.isEnabled()) {
            AlertDialog.Builder builder = new AlertDialog.Builder(MainActivity.this);
            // I'm using fragment here so I'm using getView() to provide ViewGroup
            // but you can provide here any other instance of ViewGroup from your Fragment / Activity
            View viewInflated = LayoutInflater.from(this).inflate(R.layout.dialog_signin, (ViewGroup) findViewById(android.R.id.content), false);
            // Set up the input
            //final EditText input = (EditText) viewInflated.findViewById(R.id.input);
            send_username = (EditText) viewInflated.findViewById(R.id.send_username);
            send_userid = (EditText) viewInflated.findViewById(R.id.send_userid);
            sendMessage = (EditText) viewInflated.findViewById(R.id.send_message);
            // Specify the type of input expected; this, for example, sets the input as a password, and will mask the text
            builder.setView(viewInflated);

            // Set up the buttons
            builder.setPositiveButton(android.R.string.ok, new DialogInterface.OnClickListener() {
                @Override
                public void onClick(DialogInterface dialog, int which) {
                        if((sendMessage.getText().length() == 0) || (send_username.getText().length() == 0) || (send_userid.getText().length() == 0)){
                                toast_send = Toast.makeText(MainActivity.this, "Please fill up all fields!", Toast.LENGTH_SHORT);
                                toast_send.show();
                        }else{
                            String string = sendMessage.getText().toString() + " from " + send_username.getText().toString() + " id#" + send_userid.getText().toString();
                            serialPort.write(string.getBytes());
                            tvAppend(textView, "\nINFO:\n" + string + "\n");
                            dialog.dismiss();
                        }
                }
            });
            builder.setNegativeButton(android.R.string.cancel, new DialogInterface.OnClickListener() {
                @Override
                public void onClick(DialogInterface dialog, int which) {
                    dialog.cancel();
                }
            });

            builder.show();
        }else {
            //do nothing
        }
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
        //doing nothing on pressing Back key
        return;
    }
}