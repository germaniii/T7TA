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

    String SMP;
    byte[][] sendTextBytes;
    String SID, RID, MESSAGE;
    String MESSAGE_FINAL_2, HK2;
    String textPacket;
    boolean isDisabled = false;

    static int repTimer = 0; // max of 2
    static boolean isReceivedConfirmationByte = false;

    byte[] cipherText;
    String hash;

    Context context;
    HashProcessor hashProcessor = new HashProcessor();
    PacketHandler packetHandler = new PacketHandler();
    EncryptionProcessor encryptionProcessor = new EncryptionProcessor();
    EncryptionProcessor decryptionProcessor = new EncryptionProcessor();
    int packetNumber, totalpackets;


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
                        //text = String.valueOf(Integer.parseInt(text.substring(0,text.length())) + 1);
                        // To increment SMP

                        //   New format:
                        //   | SMP - 1 | RID - 4 | SID - 4 | DATA - 40 | HK - 11 |

                        //if message entered is less than 40 characters, add whitespace characters to fill up the packet.
                        if (MESSAGE.length() < 32) {
                            for (int i = 0; i < (31 - MESSAGE.length()); i++)
                                MESSAGE_FINAL = MESSAGE_FINAL.concat(".");
                            totalpackets = 1;

                            encryptionProcessor.sendingEncryptionProcessor(MESSAGE_FINAL,SID,RID);
                            cipherText = encryptionProcessor.getCipherText();
                            packetHandler.setSendParameters(SID,RID);
                            sendTextBytes = new byte[1][60];
                            SMP = "8";

                            System.arraycopy(SMP.getBytes(), 0, sendTextBytes[0], 0, 1);
                            System.arraycopy(packetHandler.getRIDBytes(), 0, sendTextBytes[0], 1, 4);
                            System.arraycopy(packetHandler.getSIDBytes(), 0, sendTextBytes[0], 5, 4);
                            System.arraycopy(cipherText, 0, sendTextBytes[0], 9, 32);
                            System.arraycopy("........".getBytes(), 0, sendTextBytes[0], 41, 8);

                            String string = new String(sendTextBytes[0], StandardCharsets.UTF_8);
                            hash = hashProcessor.getHash(string);
                            string += hash;

                            System.arraycopy(hash.getBytes(), 0, sendTextBytes[0], 49, 11);

                            MainActivity.serialPort.write(sendTextBytes[0]);
                            tvAppend(textView, "\n\nPacket: " + string + "\nPacketLen: " + (SMP.getBytes().length+packetHandler.getRIDBytes().length+packetHandler.getSIDBytes().length+cipherText.length+hash.getBytes().length) + "\nCipher: " + new String(cipherText, StandardCharsets.UTF_8) + "\nCipherLen: " + cipherText.length+ "\nHash: " + hash);
                            SMP = String.valueOf(SMP) + 1;
                        }else if(MESSAGE.length() < 80){
                            for (int i = 0; i < (79 - MESSAGE.length()); i++)
                                MESSAGE_FINAL = MESSAGE_FINAL.concat(".");
                            packetNumber = 0;
                            totalpackets = 2;
                            sendTextBytes = new byte[totalpackets][60];

                            encryptionProcessor.sendingEncryptionProcessor(MESSAGE_FINAL,SID,RID);
                            cipherText = encryptionProcessor.getCipherText();
                            packetHandler.setSendParameters(SID,RID);

                            for(byte i = 0; i < totalpackets; i++) {
                                if(i == (totalpackets-1))
                                    SMP = "8";
                                System.arraycopy(SMP.getBytes(), 0, sendTextBytes[i], 0, 1);
                                System.arraycopy(packetHandler.getRIDBytes(), 0, sendTextBytes[i], 1, 4);
                                System.arraycopy(packetHandler.getSIDBytes(), 0, sendTextBytes[i], 5, 4);
                                System.arraycopy(cipherText, i*40, sendTextBytes[i], 9, 40);

                                String string = new String(sendTextBytes[i], StandardCharsets.UTF_8);
                                hash = hashProcessor.getHash(string);
                                string += hash;
                                System.arraycopy(hash.getBytes(), 0, sendTextBytes[i], 49, 11);

                                tvAppend(textView, "\n\nPacket " + i + ": " + string + "\nPacketLen: " + (SMP.getBytes().length+packetHandler.getRIDBytes().length+packetHandler.getSIDBytes().length+cipherText.length+hash.getBytes().length) + "\nCipher: " + new String(cipherText, StandardCharsets.UTF_8) + "\nCipherLen: " + cipherText.length + "\nHash: " + hash);
                                SMP = String.valueOf(SMP) + 1;
                            }
                            isDisabled = true;
                            resendTimer.start();

                        }else if(MESSAGE.length() <= 160){
                            for (int i = 0; i < (160 - MESSAGE.length()); i++)
                                MESSAGE_FINAL = MESSAGE_FINAL.concat(".");
                            packetNumber = 0;
                            totalpackets = 4;
                            sendTextBytes = new byte[totalpackets][60];

                            encryptionProcessor.sendingEncryptionProcessor(MESSAGE_FINAL,SID,RID);
                            cipherText = encryptionProcessor.getCipherText();
                            packetHandler.setSendParameters(SID,RID);

                            for(int i = 0; i < totalpackets; i++) {
                                if(i == (totalpackets-1))
                                    SMP = "8";

                                System.arraycopy(SMP.getBytes(), 0, sendTextBytes[i], 0, 1);
                                System.arraycopy(packetHandler.getRIDBytes(), 0, sendTextBytes[i], 1, 4);
                                System.arraycopy(packetHandler.getSIDBytes(), 0, sendTextBytes[i], 5, 4);
                                System.arraycopy(cipherText, i*40, sendTextBytes[i], 9, 40);

                                String string = new String(sendTextBytes[i], StandardCharsets.UTF_8);
                                hash = hashProcessor.getHash(string);
                                string += hash;

                                System.arraycopy(hash.getBytes(), 0, sendTextBytes[i], 49, 11);
                                tvAppend(textView, "\n\nPacket " + i + ": " + string + "\nPacketLen: " + (SMP.getBytes().length+packetHandler.getRIDBytes().length+packetHandler.getSIDBytes().length+cipherText.length+hash.getBytes().length) + "\nCipher: " + new String(cipherText, StandardCharsets.UTF_8) + "\nCipherLen: " + cipherText.length + "\nHash: " + hash);
                                SMP = String.valueOf(SMP) + 1;
                            }
                            isDisabled = true;
                            resendTimer.start();
                        } else if(MESSAGE.length() < 240){
                            for (int i = 0; i < (239 - MESSAGE.length()); i++)
                                MESSAGE_FINAL = MESSAGE_FINAL.concat(".");
                            packetNumber = 0;
                            totalpackets = 240/40;
                            sendTextBytes = new byte[totalpackets][60];

                            encryptionProcessor.sendingEncryptionProcessor(MESSAGE_FINAL,SID,RID);
                            cipherText = encryptionProcessor.getCipherText();
                            packetHandler.setSendParameters(SID,RID);

                            for(int i = 0; i < totalpackets; i++) {
                                if(i == (totalpackets-1))
                                    SMP = "8";
                                else
                                    SMP = String.valueOf(SMP) + 1;
                                System.arraycopy(SMP, 0, sendTextBytes[i], 0, 1);
                                System.arraycopy(packetHandler.getRIDBytes(), 0, sendTextBytes[i], 1, 4);
                                System.arraycopy(packetHandler.getSIDBytes(), 0, sendTextBytes[i], 5, 4);
                                System.arraycopy(cipherText, i*40, sendTextBytes[i], 9, 40);

                                String string = new String(sendTextBytes[i], StandardCharsets.UTF_8);
                                hash = hashProcessor.getHash(string);
                                string += hash;

                                System.arraycopy(hash.getBytes(), 0, sendTextBytes[i], 49, 11);
                                tvAppend(textView, "\n\nPacket " + i + ": " + string + "\nPacketLen: " + (SMP.getBytes().length+packetHandler.getRIDBytes().length+packetHandler.getSIDBytes().length+cipherText.length+hash.getBytes().length) + "\nCipher: " + new String(cipherText, StandardCharsets.UTF_8) + "\nCipherLen: " + cipherText.length + "\nHash: " + hash);
                            }
                            isDisabled = true;
                            resendTimer.start();

                        }

                        //Should use the serial port from MainActivity to reference the registered serialPort Arduino
                        //MainActivity.serialPort.write((textPacket).getBytes());
                        // prevent multiple send touches

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
            Toast.makeText(context, "Please Connect EMA Device", Toast.LENGTH_SHORT).show();
            tvAppend(textView, "\n" + e.toString());

        }
    }

     CountDownTimer resendTimer = new CountDownTimer(2000, 1000) {
        @Override
        public void onTick(long l) {
            if (isReceivedConfirmationByte) {// this will stop the counting
                repTimer = 4;
                packetNumber = 0;
                isReceivedConfirmationByte = false;
                resendTimer.cancel();
            }

        }

        @Override
        public void onFinish() {
            try {
                if (packetNumber < totalpackets) {
                    Toast.makeText(context, "Sent Packet " + (packetNumber + 1) + "/" + sendTextBytes.length +" try " + (repTimer+1), Toast.LENGTH_SHORT).show();
                    MainActivity.serialPort.write(sendTextBytes[packetNumber]);
                    packetNumber++;
                    resendTimer.cancel();
                    resendTimer.start();
                } else {
                    packetNumber = 0;
                    resendTimer.cancel();
                }
                if (packetNumber == totalpackets) {
                    countDownRepeater();
                    packetNumber = 0;
                }
            }catch (Exception e){
                Toast.makeText(context, "Please connect EMA device!", Toast.LENGTH_SHORT).show();
            }
        }
    };

    private void countDownRepeater(){
        if (repTimer == 4){
            resendTimer.cancel();
            isDisabled = false;
            repTimer = 0;
            packetNumber = 0;
            Toast.makeText(context, "Successfully sent message to "
                    + RID, Toast.LENGTH_SHORT).show();

            //if message is longer than 44 characters, add a function here to send the next packet.
        }else if(repTimer < 3){
            repTimer++;
            resendTimer.cancel();
            resendTimer.start();
        }else if (repTimer == 3){
            // if needed, add a notification part here
            isDisabled = false;
            resendTimer.cancel();
            repTimer = 0;
            Toast.makeText(context, "Failed to send message to " + RID, Toast.LENGTH_SHORT).show();
        }else{
            repTimer = 0;
            isDisabled = false;
            resendTimer.cancel(); // for error trapping (stop the loop)
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
                    tvAppend(textView, "\nName:" + number.getSelectedItem().toString() + RID);
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