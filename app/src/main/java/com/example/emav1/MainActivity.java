package com.example.emav1;

import androidx.annotation.RequiresApi;
import androidx.appcompat.app.AppCompatActivity;
import androidx.appcompat.app.AppCompatDelegate;
import androidx.core.content.ContextCompat;
import androidx.fragment.app.FragmentManager;
import androidx.fragment.app.FragmentTransaction;

import android.app.AlertDialog;
import android.app.PendingIntent;
import android.content.BroadcastReceiver;
import android.content.Context;
import android.content.Intent;
import android.content.IntentFilter;
import android.database.Cursor;
import android.hardware.usb.UsbDevice;
import android.hardware.usb.UsbDeviceConnection;
import android.hardware.usb.UsbManager;
import android.media.MediaPlayer;
import android.media.RingtoneManager;
import android.net.Uri;
import android.os.Build;
import android.os.Bundle;
import android.text.method.ScrollingMovementMethod;
import android.util.Log;
import android.view.View;
import android.widget.ImageButton;
import android.widget.TextView;
import android.widget.Toast;

import com.example.emav1.toolspack.PacketHandler;
import com.felhr.usbserial.UsbSerialDevice;
import com.felhr.usbserial.UsbSerialInterface;

import java.util.HashMap;
import java.util.Map;

public class MainActivity extends AppCompatActivity{

    public final String ACTION_USB_PERMISSION = "com.example.emav1.USB_PERMISSION";
    TextView textView;
    UsbManager usbManager;
    public UsbDevice device;
    static UsbSerialDevice serialPort;
    public UsbDeviceConnection connection;
    ImageButton  beacon, toTextMode, toContactList, toReceiverMode;

    PacketHandler packetHandler;

    DataBaseHelper dataBaseHelper;

    FragmentManager fragmentManager;
    FragmentTransaction fragmentTransaction;

    //navbar switches
    boolean isReceiverMode;
    boolean isContactList;
    boolean isTextMessageMode;

    // Serial Receiver Variables
    private String data;
    private byte[] stream;

    private Uri notificationSound = RingtoneManager.getDefaultUri(RingtoneManager.TYPE_NOTIFICATION);
    private MediaPlayer mp;

    /*
    This and onClickBeaconMode are the only functions you need to touch in this class.
    Mao rani ang hilabti if mag manipulate mo sa data nga ma receive from the EMA device.

    This is where all the data passed to and from the EMA device is processed.

    To implement:
        - Twofish Algorithm
        - JH?
        - Check if signal is emergency, and play the emergency sound in R.raw
     */
    UsbSerialInterface.UsbReadCallback mCallback = new UsbSerialInterface.UsbReadCallback() { //Defining a Callback which triggers whenever data is read.
        @Override
        public void onReceivedData(byte[] arg0) {
            String num = getUserSID().trim();
            try {
                stream = arg0; // assign the received data from arduino to stream variable
                if(stream == null); // do nothing if nothing is received

                data = new String(arg0, "UTF-8");

                // Control Code 1, Send SID to Arduino Device
                if(stream[0] == 1) {
                    tvAppend(textView, "Received : " + stream[0] + "\n");
                    serialPort.write(num.getBytes());
                    tvAppend(textView, "OutStream : " + num + "\n");
                }else if(stream[0] == 0) {
                    mp = MediaPlayer.create(MainActivity.this, R.raw.emergency_alarm);
                    mp.start(); // Play sound
                    char[] sender = new char[4];
                    data.getChars(5,8,sender,0);    // Extract SID of the received packet
                    new AlertDialog.Builder(MainActivity.this)
                            .setIcon(R.drawable.icon_ema)
                            .setTitle("Emergency Signal Detected!")
                            .setMessage("Emergency Signal from user " + sender.toString())
                            .setPositiveButton("Ok",null)
                            .show();

                }else{
                    // ... decryption for display, and store it in a temporary string.
                    // ... notification function
                    // ... store to messages table in database encrypted
                    // if(regular message)
                    mp = MediaPlayer.create(MainActivity.this, notificationSound);
                    mp.start(); // Play sound
                }

                tvAppend(textView, "InStream : " + data);
                //tvAppend(textView, Arrays.toString(stream) + "\n");
            } catch (Exception e) {
                Toast.makeText(MainActivity.this, "Receive Error", Toast.LENGTH_SHORT).show();

            }
        }
    };

    // This function handles what happens when the beacon mode button is clicked.
    public void onClickBeaconMode(View view){
        try{
            if(beacon.isEnabled())
                for(int i=5; i>0; i--) {
                    String string = "0" + "0000"+ getUserSID() + "00000" + "00000" + "00000" +
                            "00000" + "00000" + "00000" + "00000" + "00000" + "00000" + "1234567890"; // <-- this HK part will be replaced later on when HK algorithm is finished
                    /*
                        The 'string' is similar to the packet assignment mentioned in the Manuscript
                        | SMP-1 | RID-4 | SID-4 | DATA-40 | HK-10 |  ----> This totals to 64bytes-1packet
                     */
                    serialPort.write(string.getBytes());
                    tvAppend(textView, "\nINFO:\n" + string + "\n");
                }
        }catch(Exception e){
            Toast.makeText(MainActivity.this, "Please Connect the EMA Device!", Toast.LENGTH_SHORT).show();
        }

    }

    @RequiresApi(api = Build.VERSION_CODES.N)
    @Override
    protected void onCreate(Bundle savedInstanceState) {
        AppCompatDelegate.setDefaultNightMode(AppCompatDelegate.MODE_NIGHT_NO);     // Only use light mode
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_main);

        usbManager = (UsbManager) getSystemService(this.USB_SERVICE);
        textView = findViewById(R.id.main_serialMonitor);
        beacon = findViewById(R.id.main_beaconButton);
        toTextMode = findViewById(R.id.toTextModeButton);
        toContactList = findViewById(R.id.toContactList);
        toReceiverMode = findViewById(R.id.toReceiverModeButton);
        textView.setMovementMethod(new ScrollingMovementMethod());
        packetHandler = new PacketHandler();

        dataBaseHelper = new DataBaseHelper(this);

        IntentFilter filter = new IntentFilter();
        filter.addAction(ACTION_USB_PERMISSION);
        filter.addAction(UsbManager.ACTION_USB_DEVICE_ATTACHED);
        filter.addAction(UsbManager.ACTION_USB_DEVICE_DETACHED);
        registerReceiver(broadcastReceiver, filter);
        textView.setMovementMethod(new ScrollingMovementMethod());
        setReceiverModeColor();

        // Set Beacon Image Whenever Transmission Device is Connected
        if (!arduinoConnected()) {
            //beacon.setImageResource(R.drawable.icon_beacon_on);
        }
    }

    // This initializes the broadcast receiver whenever the EMA Device is connected to the phone.
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
                Toast.makeText(MainActivity.this, "EMA device connected!", Toast.LENGTH_SHORT).show();
            } else if (intent.getAction().equals(UsbManager.ACTION_USB_DEVICE_DETACHED)) {
                arduinoDisconnected();
                Toast.makeText(MainActivity.this, "EMA device disconnected!", Toast.LENGTH_SHORT).show();
            }
        }
    };

    // This function is called whenever the EMA device is connected
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


    /*
     This function handles the changing of the ui
     from Contact List to Inbox List and Text Message Mode
     */
    public void ChangeFragment(View view){
        if (view == findViewById(R.id.toContactList) && !isContactList) {
            fragmentManager = getSupportFragmentManager();
            fragmentTransaction = fragmentManager.beginTransaction();
            fragmentTransaction.setReorderingAllowed(true)
                    .setCustomAnimations(
                            R.anim.slide_to_right,  // enter
                            R.anim.fade_out  // popExit
                    )
                    .remove(this.getSupportFragmentManager().findFragmentById(R.id.fragment_container_view))
                    .replace(R.id.fragment_container_view, FragmentContactList.class, null)
                    .commit();

            //set navbar switches
            setContactListColor();

        }

        if(view == findViewById(R.id.toTextModeButton) && !isTextMessageMode){
            fragmentManager = getSupportFragmentManager();
            fragmentTransaction = fragmentManager.beginTransaction();
            fragmentTransaction.setReorderingAllowed(true)
                    .setCustomAnimations(
                            R.anim.slide_to_left,  // enter
                            R.anim.fade_out // popExit
                    )
                    .remove(this.getSupportFragmentManager().findFragmentById(R.id.fragment_container_view))
                    .replace(R.id.fragment_container_view, FragmentTextMessage.class, null)
                    .commit();

            //set navbar switches
            setTextMessageColor();
        }

        if(view == findViewById(R.id.toReceiverModeButton) && !isReceiverMode){
            fragmentManager = getSupportFragmentManager();
            fragmentTransaction = fragmentManager.beginTransaction();
            fragmentTransaction.setReorderingAllowed(true)
                    .setCustomAnimations(
                            R.anim.slide_to_down,  // enter
                            R.anim.fade_out // popExit
                    )
                    .remove(this.getSupportFragmentManager().findFragmentById(R.id.fragment_container_view))
                    .replace(R.id.fragment_container_view, FragmentMain.class, null)
                    .commit();

            //set navbar switches
            setReceiverModeColor();
        }
    }

    //for use when changing fragments to change the navbar colors
    public void setContactListColor(){
        toContactList.setColorFilter(ContextCompat.getColor(this, R.color.bluegreen));
        toReceiverMode.setColorFilter(ContextCompat.getColor(this, R.color.white));
        toTextMode.setColorFilter(ContextCompat.getColor(this, R.color.white));
        isContactList = true;
        isReceiverMode = false;
        isTextMessageMode = false;

    }
    public void setReceiverModeColor(){
        toContactList.setColorFilter(ContextCompat.getColor(this, R.color.white));
        toReceiverMode.setColorFilter(ContextCompat.getColor(this, R.color.bluegreen));
        toTextMode.setColorFilter(ContextCompat.getColor(this, R.color.white));
        isContactList = false;
        isReceiverMode = true;
        isTextMessageMode = false;
    }
    public void setTextMessageColor(){
        toContactList.setColorFilter(ContextCompat.getColor(this, R.color.white));
        toReceiverMode.setColorFilter(ContextCompat.getColor(this, R.color.white));
        toTextMode.setColorFilter(ContextCompat.getColor(this, R.color.bluegreen));
        isContactList = false;
        isReceiverMode = false;
        isTextMessageMode = true;
    }

    // This is a database handler to get the User SID whenever the Arduino is connected.
    String getUserSID(){
        String SID = null;
        Cursor cursor;
        cursor = dataBaseHelper.readUserSID();

        if (cursor.getCount() == 0) {
            Toast.makeText(MainActivity.this, "No User SID!", Toast.LENGTH_SHORT).show();
        } else {
            if(cursor.moveToFirst()){
                SID = cursor.getString(0);     //CONTACT NUM
                    while(cursor.moveToNext())
                        SID = cursor.getString(0);     //CONTACT NUM
            }
        }
        return SID;
    }

    // This will be called in FragmentTextMessage and mCallback to store messages to database.
    @RequiresApi(api = Build.VERSION_CODES.N)
    private void storeMessage(){
        String SID = getUserSID().toString().trim();
        String Message = String.copyValueOf(stream.toString().toCharArray(), 9, 44);
        String Received = FragmentMain.dateFormat.format(FragmentMain.date);
        String Sent = "-";

        dataBaseHelper.addOneMessage(SID, Message, Received,Sent);

        //refill the contact Array lists so that the Contact ID will be filled with the new information
        FragmentMain.messageID.clear();
        FragmentMain.messageNames.clear();
        FragmentMain.messageNum.clear();
        FragmentMain.messageText.clear();
        FragmentMain.messageSent.clear();
        FragmentMain.messageReceived.clear();
        FragmentMain.storeDBtoArrays();

        FragmentMain.inboxListAdapter.notifyDataSetChanged();

    }

    public void onBackPressed() {
        //doing nothing on pressing Back key
    }
}