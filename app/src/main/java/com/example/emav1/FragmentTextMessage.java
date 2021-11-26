package com.example.emav1;

import android.content.Context;
import android.database.Cursor;
import android.hardware.camera2.TotalCaptureResult;
import android.hardware.usb.UsbDevice;
import android.hardware.usb.UsbDeviceConnection;
import android.hardware.usb.UsbManager;
import android.os.Build;
import android.os.Bundle;

import androidx.annotation.NonNull;
import androidx.annotation.Nullable;
import androidx.annotation.RequiresApi;
import androidx.fragment.app.Fragment;

import android.os.CountDownTimer;
import android.os.Handler;
import android.view.LayoutInflater;
import android.view.View;
import android.view.ViewGroup;
import android.widget.ArrayAdapter;
import android.widget.EditText;
import android.widget.ImageButton;
import android.widget.Spinner;
import android.widget.TextView;
import android.widget.Toast;

import com.example.emav1.toolspack.EncryptionProcessor;
import com.example.emav1.toolspack.HashProcessor;
import com.example.emav1.toolspack.PacketHandler;
import com.felhr.usbserial.UsbSerialDevice;

import java.nio.charset.StandardCharsets;
import java.util.ArrayList;

public class FragmentTextMessage extends Fragment {

    public final String ACTION_USB_PERMISSION = "com.example.emav1.USB_PERMISSION";
    TextView textView;
    UsbManager usbManager;
    UsbDevice device;
    UsbSerialDevice serialPort;
    UsbDeviceConnection connection;
    ImageButton sendButton;
    EditText message;
    Spinner number;
    ArrayList<String> spinnerContacts;
    DataBaseHelper dataBaseHelper;

    String SMP, SID, RID, MESSAGE;
    String MESSAGE_FINAL_2, HK2;
    String textPacket;
    boolean isDisabled = false;

    static int repTimer = 0; // max of 2
    static boolean isReceivedConfirmationByte = false;

    byte[][] dividedCipherText;
    byte[][] hash;
    byte[][] toSendPacket;

    Context context;
    HashProcessor hashProcessor = new HashProcessor();
    PacketHandler packetHandler = new PacketHandler();
    EncryptionProcessor encryptionProcessor = new EncryptionProcessor();
    EncryptionProcessor decryptionProcessor = new EncryptionProcessor();
    int packetNumber;

    /*final Handler handler = new Handler();

    Runnable sendTextMessage = new Runnable() {
        @Override
        public void run() {
            for(int i = 0; i < 10; i++){
                if(){

                }
            }
            handler.postDelayed(this, 0);  // 1 second delay
        }
    };

    Runnable sendPacket = new Runnable() {
        @Override
        public void run() {
            MainActivity.serialPort.write(textPacket.getBytes());
            if (repTimer == 4){
                isDisabled = false;
                repTimer = 0;
                Toast.makeText(context, "Successfully sent message to "
                        + RID, Toast.LENGTH_SHORT).show();
                handler.removeCallbacks(this);

                //if message is longer than 44 characters, add a function here to send the next packet.
            }else if(repTimer < 3){
                repTimer++;
                handler.postDelayed(this, 1000);
            }else if (repTimer == 3){
                // if needed, add a notification part here
                isDisabled = false;
                repTimer = 0;
                Toast.makeText(context, "Failed to send message to " + RID, Toast.LENGTH_SHORT).show();
            }else{
                repTimer = 0;
                isDisabled = false;
                handler.removeCallbacks(this);
            }
        }
    };
    handler.post(runnable);
     */

    /*
    This is the only thing you need to touch in this class.
    This handles when the Send Button is being pressed.

    To implement:
        - Background process of sending?
        - if not, Loading screen to wait until all the message packets are successfully sent?

     */
    @RequiresApi(api = Build.VERSION_CODES.KITKAT)
    public void onClickSendButton(View view) {
        try {
            if (sendButton.isEnabled()){
                if ((message.getText().length() == 0) || number.getSelectedItem() == "") {
                    Toast.makeText(context, "Please Fill Up All Fields!", Toast.LENGTH_SHORT).show();
                }else {
                    if(!isDisabled) {
                        SMP = "3";
                        getSID();
                        getRID();
                        String MESSAGE = message.getText().toString().trim();
                        String MESSAGE_FINAL = MESSAGE;
                        String HK;

                        //text = String.valueOf(Integer.parseInt(text.substring(0,text.length())) + 1);
                        // To increment SMP

                        //   New format:
                        //   | SMP - 1 | RID - 4 | SID - 4 | DATA - 40 | HK - 11 |

                        //if message entered is less than 40 characters, add whitespace characters to fill up the packet.
                        if (MESSAGE.length() < 40) {
                            for (int i = 0; i < 40 - MESSAGE.length(); i++)
                                MESSAGE_FINAL = MESSAGE_FINAL.concat(".");
                        }

                        String textMessage = SMP+RID+SID+MESSAGE_FINAL;
                        encryptionProcessor.sendingEncryptionProcessor(MESSAGE, SID, RID);
                        dividedCipherText = encryptionProcessor.getDividedCipherText();
                        hash = new byte[encryptionProcessor.getPacketTotal()][11];
                        toSendPacket = new byte[encryptionProcessor.getPacketTotal()][60];
                        packetHandler.setSID(SID);
                        packetHandler.setRID(RID);


                        /*byte[] sendBytes = new byte[60];
                        packetHandler.getSenderID();
                        System.arraycopy("00000".getBytes(), 0, sendBeaconBytes, 0, 5);
                        System.arraycopy(packetHandler.getSIDBytes(), 0, sendBeaconBytes, 5, 4);
                        System.arraycopy("0000000000000000000000000000000000000000".getBytes(), 0, sendBeaconBytes, 9, 40);

                        String string = "0" + "0000" + new String(packetHandler.getSIDBytes(), StandardCharsets.UTF_8)+ "00000" + "00000" + "00000" + // Data
                                "00000" + "00000" + "00000" + "00000" + "00000"; // + "123456" + "78911"

                        String beaconHash = hashProcessor.getHash(string);
                        String beaconMessage = string + beaconHash;

                        System.arraycopy(beaconHash.getBytes(), 0, sendBeaconBytes, 49, 11);

                         */


                        for(int i = 0; i < encryptionProcessor.getPacketTotal(); i++){
                            byte[] tempPacket = new byte[49];
                            System.arraycopy(String.valueOf(i+3).getBytes(), 0, tempPacket, 0,1);
                            System.arraycopy(packetHandler.getSIDBytes(), 0, tempPacket, 1, 4);
                            System.arraycopy(packetHandler.getRIDBytes(), 0, tempPacket, 5, 4);
                            System.arraycopy(dividedCipherText[i], 0, tempPacket, 9, 40);
                            hash[i] = hashProcessor.getHash(new String(tempPacket, StandardCharsets.UTF_8)).getBytes();
                        }

                        packetHandler.setSendParameters(SID, RID, dividedCipherText, hash);

                        toSendPacket = packetHandler.getPacketsForSending();
                        packetNumber = packetHandler.getNumOfPackets();

                        tvAppend(textView, "\n\n***Sending***" +
                                "\nSID : " + packetHandler.getSenderID() +
                                "\nRID : " + packetHandler.getReceiverID() +
                                "\nEncryptedMessage: " +
                                "\nHash : ---" );


                        countDownTimer.start();
                        //Should use the serial port from MainActivity to reference the registered serialPort Arduino
                        //MainActivity.serialPort.write((textPacket).getBytes());
                        Toast.makeText(context, "Transmitted", Toast.LENGTH_SHORT).show();

                        // prevent multiple send touches
                        isDisabled = true;

                        //Start repitition Counter

                        //handler.postRunnable
                    }else{
                        Toast.makeText(context, "A message is still sending, please try again later.", Toast.LENGTH_SHORT).show();
                    }

                    //Countdown timer to wait for variable change (confirmation byte received.)
                    // Max repetition would be 3? times
                }
            }else
                Toast.makeText(context, "Synchronizing EMA Device, Please Wait", Toast.LENGTH_SHORT).show();
        }catch (Exception e){
            //Toast.makeText(context, "Please Connect EMA Device", Toast.LENGTH_SHORT).show();
            tvAppend(textView, e.toString());
        }
    }

     CountDownTimer countDownTimer = new CountDownTimer(1000, 1000) {
        @Override
        public void onTick(long l) {
            if (isReceivedConfirmationByte)// this will stop the counting
                repTimer = 4;

        }

        @Override
        public void onFinish() {
            if(packetNumber < packetHandler.getNumOfPackets()){
                Toast.makeText(context, "Sent Packet [" + (packetNumber + 1) + " / " + toSendPacket.length +"]", Toast.LENGTH_SHORT).show();
                MainActivity.serialPort.write(toSendPacket[packetNumber]);
                packetNumber++;
                countDownTimer.cancel();
                countDownTimer.start();
            }else{
                packetNumber = 0;
                countDownTimer.cancel();
            }
            if(packetNumber == packetHandler.getNumOfPackets()) {
                countDownRepeater();
                packetNumber = 0;
            }
        }
    };

    private void countDownRepeater(){
        if (repTimer == 4){
            countDownTimer.cancel();
            isDisabled = false;
            repTimer = 0;
            packetNumber = 0;
            Toast.makeText(context, "Successfully sent message to "
                    + RID, Toast.LENGTH_SHORT).show();

            //if message is longer than 44 characters, add a function here to send the next packet.
        }else if(repTimer < 3){
            repTimer++;
            countDownTimer.cancel();
            countDownTimer.start();
        }else if (repTimer == 3){
            // if needed, add a notification part here
            isDisabled = false;
            countDownTimer.cancel();
            repTimer = 0;
            Toast.makeText(context, "Failed to send message to " + RID, Toast.LENGTH_SHORT).show();
        }else{
            repTimer = 0;
            isDisabled = false;
            countDownTimer.cancel(); // for error trapping (stop the loop)
        }
    }

    @Override
    public View onCreateView(LayoutInflater inflater, ViewGroup container,
                             Bundle savedInstanceState) {
        // Inflate the layout for this fragment
        return inflater.inflate(R.layout.fragment_text_message, container, false);
    }

    @Override
    public void onViewCreated(@NonNull View view, @Nullable @org.jetbrains.annotations.Nullable Bundle savedInstanceState) {
        super.onViewCreated(view, savedInstanceState);
        this.context = getActivity();

        textView = getActivity().findViewById(R.id.main_serialMonitor);
        sendButton = getActivity().findViewById(R.id.textMessage_sendButton);
        message = getActivity().findViewById(R.id.textMessage_message);

        dataBaseHelper = new DataBaseHelper(context);
        spinnerContacts = new ArrayList<>();
        storeDBtoArrays();


        number = getActivity().findViewById(R.id.textMessage_Spinner);
        ArrayAdapter<String> adapter = new ArrayAdapter<>(context,
                android.R.layout.simple_spinner_item, spinnerContacts);
        adapter.setDropDownViewResource(android.R.layout.simple_spinner_dropdown_item);
        number.setAdapter(adapter);

        // Send Button On Click Listener
        sendButton.setOnClickListener(new View.OnClickListener()
        {
            @RequiresApi(api = Build.VERSION_CODES.KITKAT)
            @Override
            public void onClick(View v)
            {
                onClickSendButton(v);
            }
        });

    }


    // This function retrieves the contact number (SID) of the user.
    void getSID(){
        Cursor cursor = dataBaseHelper.readUserSID();
        if(cursor.getCount() < 1){
            Toast.makeText(context, "Error Getting SID", Toast.LENGTH_SHORT).show();
        }else{
            if(cursor.moveToFirst()){
                    SID = cursor.getString(0);  //Names
                    tvAppend(textView, SID+"\n");
            }
        }
    }

    // This function retrieves the RID of the contact that is selected.
    void getRID(){
        Cursor cursor = dataBaseHelper.readContactNumber(number.getSelectedItem().toString());
        if(cursor.getCount() < 1){
            Toast.makeText(context, "Error Getting RID", Toast.LENGTH_SHORT).show();
        }else{
            if(cursor.moveToFirst()){
                    RID = cursor.getString(0);  //Names
            }
                    tvAppend(textView, "Name:" + number.getSelectedItem().toString() + RID+"\n");
        }
    }


    void storeDBtoArrays(){
        Cursor cursor = dataBaseHelper.readAllDataContactsTable();
        if(cursor.getCount() <= 1){
            Toast.makeText(context, "No Contacts Found!", Toast.LENGTH_SHORT).show();
        }else{
            if(cursor.moveToFirst()){
                while (cursor.moveToNext()){
                    spinnerContacts.add(cursor.getString(1));  //Names
                }
            }
        }
    }

    private void tvAppend(TextView tv, CharSequence text) {
        final TextView ftv = tv;
        final CharSequence ftext = text;

        getActivity().runOnUiThread(new Runnable() {
            @Override
            public void run() {
                ftv.append(ftext);
            }
        });
    }



}