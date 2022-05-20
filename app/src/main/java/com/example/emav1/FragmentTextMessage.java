package com.example.emav1;

import android.content.Context;
import android.database.Cursor;
import android.graphics.Color;
import android.hardware.camera2.TotalCaptureResult;
import android.hardware.usb.UsbDevice;
import android.hardware.usb.UsbDeviceConnection;
import android.hardware.usb.UsbManager;
import android.os.Build;
import android.os.Bundle;

import androidx.annotation.MainThread;
import androidx.annotation.NonNull;
import androidx.annotation.Nullable;
import androidx.annotation.RequiresApi;
import androidx.fragment.app.Fragment;

import android.os.CountDownTimer;
import android.os.Handler;
import android.util.Base64;
import android.util.Log;
import android.view.LayoutInflater;
import android.view.View;
import android.view.ViewGroup;
import android.widget.ArrayAdapter;
import android.widget.EditText;
import android.widget.ImageButton;
import android.widget.Spinner;
import android.widget.TextView;
import android.widget.Toast;

import com.example.emav1.toolspack.*;
import com.felhr.usbserial.UsbSerialDevice;

import org.bouncycastle.jcajce.provider.asymmetric.ec.KeyAgreementSpi;

import java.math.BigDecimal;
import java.math.BigInteger;
import java.nio.charset.StandardCharsets;
import java.util.ArrayList;
import java.util.Arrays;
import java.util.Random;

public class FragmentTextMessage extends Fragment {

    ImageButton sendButton;
    EditText message;
    Spinner number;
    ArrayList<String> spinnerContacts;
    DataBaseHelper dataBaseHelper;

    byte[] smp = new byte[1];
    byte[][] sendTextBytes;
    String SID, RID;
    static String MESSAGE;
    String MESSAGE_FINAL_2, HK2;
    String textPacket;
    boolean isDisabled = false;

    static int repTimer = 0; // max of 2
    static boolean isReceivedConfirmationByte = false;

    byte[] cipherText;
    String hash;
    String[] messageArray;

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
            if (sendButton.isEnabled()) {
                if ((message.getText().length() == 0) || number.getSelectedItem() == "") {
                    Toast.makeText(context, "Please Fill Up All Fields!", Toast.LENGTH_SHORT).show();
                } else {
                    if (!isDisabled) {
                        isDisabled = true;
                        smp[0] = 0x05;
                        getSID();
                        getRID();
                        MESSAGE = message.getText().toString().trim();
                        String MESSAGE_FINAL = MESSAGE;

                        //if message entered is less than 31 characters, add whitespace characters to fill up the packet.
                        for (int i = 0; i < (20 - MESSAGE.length() % 20); i++)
                            MESSAGE_FINAL = MESSAGE_FINAL.concat(" ");
                        totalpackets = (int) Math.ceil((double) MESSAGE_FINAL.length() / 20);
                        messageArray = new String[totalpackets];

                        for (int i = 0; i < totalpackets; i++) {
                            messageArray[i] = SID + MESSAGE_FINAL.substring((i * 20), ((i * 20) + 20));
                            //tvAppend(textView, "\nPacket " + i + ": " + messageArray[i]);
                        }
                        packetHandler.setSendParameters(SID, RID);
                        sendTextBytes = new byte[totalpackets][60];

                        //text = String.valueOf(Integer.parseInt(text.substring(0,text.length())) + 1);
                        // To increment SMP

                        //   New format:
                        //   | SMP - 1 | RID - 4 | SID - 4 | DATA - 40 | HK - 11 |
                        //   as of 11/27/2021 -- increase data
                        //  | SMP - 1 | RID - 4 | SID - 4 | DATA - 43 | HK - 8 |

                        //Send the Handshake
                        byte[] sendHandShakeBytes = new byte[60];
                        byte[] smp = new byte[1];

                        smp[0] = 0x04;
                        System.arraycopy(smp, 0, sendHandShakeBytes, 0, 1);
                        System.arraycopy(packetHandler.getRIDBytes(), 0, sendHandShakeBytes, 1, 4);
                        System.arraycopy(packetHandler.getSIDBytes(), 0, sendHandShakeBytes, 5, 4);


                        /*String tempA = new DHProtocol(MainActivity.DHPrivateKey).getPublicKey().toString();
                        for (int i = tempA.length(); i < 40; i++)
                            tempA +=  "a";

                        Log.d("DHKeys", "Generated A : " + tempA);
                         */

                        BigInteger A = new DHProtocol(MainActivity.DHPrivateKey).getPublicKey();
                        byte[] tempA = A.toByteArray();
                        Log.d("DHKeys", "BigInteger A Array : " + Arrays.toString(tempA));

                        // PADDING FUNCTION
                        byte[] paddedArr = new byte[40];
                        Arrays.fill(paddedArr, (byte) 126);

                        // COPY tempA to paddedArr
                        System.arraycopy(tempA, 0, paddedArr, 0, tempA.length);
                        Log.d("DHKeys", "BigInteger A Padded Array: "  + Arrays.toString(paddedArr));

                        System.arraycopy(paddedArr, 0, sendHandShakeBytes, 9, 40);

                        String sendHandshakeString = new String(sendHandShakeBytes, StandardCharsets.UTF_8);
                        String hash = hashProcessor.getHash(sendHandshakeString);
                        sendHandshakeString += hash;
                        System.arraycopy(hash.getBytes(), 0, sendHandShakeBytes, 52, 8);

                        MainActivity.serialPort.write(sendHandShakeBytes);
                        Log.d("DHKeys", "Sent Handshake");

                        //Check if Handshake received
                        checkIfReadyToTransmit.start();


                    } else {
                        Toast.makeText(context, "A message is still sending, please try again later.", Toast.LENGTH_SHORT).show();
                    }

                    //Countdown timer to wait for variable change (confirmation byte received.)
                    // Max repetition would be 3? times
                }
            } else
                Toast.makeText(context, "Synchronizing EMA Device, Please Wait", Toast.LENGTH_SHORT).show();
        }catch (Exception e){
            Toast.makeText(context, "Please Connect EMA Device", Toast.LENGTH_SHORT).show();
            isDisabled = false;
            resendTimer.cancel();
            resendCanceller.cancel();
            packetNumber = 0;
        }
    }

    @RequiresApi(api = Build.VERSION_CODES.KITKAT)
    CountDownTimer checkIfReadyToTransmit = new CountDownTimer(5000,1000) {
        @Override
        public void onTick(long l) {
            if(MainActivity.isReadyToTransmitTextMessage){
                //Assign Stuff
                for (byte i = 0; i < totalpackets; i++) {

                    Log.d("DHKeys","Parameter Public : " + MainActivity.DHPublicKey);
                    Log.d("DHKeys","Parameter Private : " + MainActivity.DHPrivateKey);
                    encryptionProcessor.sendingEncryptionProcessor(messageArray[i], MainActivity.DHPublicKey, MainActivity.DHPrivateKey);
                    cipherText = encryptionProcessor.getCipherText();
                    String cipherbase64 = Base64.encodeToString(cipherText, Base64.NO_WRAP | Base64.NO_PADDING | Base64.URL_SAFE | Base64.NO_CLOSE);
                    //tvAppend(textView, "\nBase64Cipher: " + cipherbase64 +
                    //         "\nBase64CipherLen: " + cipherbase64.length());

                    if (totalpackets == 1 || (i == totalpackets-1))
                        smp[0] = 0x7F;

                    System.arraycopy(smp, 0, sendTextBytes[i], 0, 1);
                    System.arraycopy(packetHandler.getRIDBytes(), 0, sendTextBytes[i], 1, 4);
                    System.arraycopy(packetHandler.getSIDBytes(), 0, sendTextBytes[i], 5, 4);
                    System.arraycopy(cipherbase64.getBytes(), 0, sendTextBytes[i], 9, 43);

                    String string = new String(sendTextBytes[i], StandardCharsets.UTF_8);
                    hash = hashProcessor.getHash(string);
                    string += hash;

                    System.arraycopy(hash.getBytes(), 0, sendTextBytes[i], 52, 8);

                    //MainActivity.serialPort.write(sendTextBytes[0]);
                    packetNumber=0;
                    //tvAppend(textView, "\n\nSENDING PACKET\n" + "\nPacket: " + string + "\nPacketLen: " + (smp.length + packetHandler.getRIDBytes().length + packetHandler.getSIDBytes().length + cipherbase64.length() + hash.getBytes().length) + "\nCipher: " + new String(cipherText, StandardCharsets.UTF_8) + "\nCipherLen: " + cipherText.length + "\nHash: " + hash);
                    smp[0]+=1;
                }
                // Start Transmission
                resendTimer.start();
                resendCanceller.start();
            }

        }

        @Override
        public void onFinish() {
            if(MainActivity.isReadyToTransmitTextMessage){
                Toast.makeText(context, "Successfully Received Handshake", Toast.LENGTH_SHORT).show();
                MainActivity.isReadyToTransmitTextMessage = false;
                isDisabled = false;
                checkIfReadyToTransmit.cancel();
            }else{
                Toast.makeText(context, "Failed to Receive Handshake, Please Send Again", Toast.LENGTH_SHORT).show();
                MainActivity.isReadyToTransmitTextMessage = false;
                isDisabled = false;
                checkIfReadyToTransmit.cancel();
            }

        }
    };

    CountDownTimer resendCanceller = new CountDownTimer(3000, 500) {
        @Override
        public void onTick(long l) {
            if(isReceivedConfirmationByte){
                resendTimer.cancel();
                repTimer = 4;
                packetNumber = 0;
                isReceivedConfirmationByte = false;
                countDownRepeater();
                isDisabled = false;
            }
        }

        @Override
        public void onFinish() {
            if(isReceivedConfirmationByte) {
                repTimer = 4;
                packetNumber = 0;
                isReceivedConfirmationByte = false;
                countDownRepeater();
                isDisabled = false;
                resendCanceller.cancel();

            }
        }
    };

     CountDownTimer resendTimer = new CountDownTimer(3000, 1000) {
        @Override
        public void onTick(long l) {
            if (isReceivedConfirmationByte) {   // this will stop the counting
                repTimer = 4;
                packetNumber = 0;
                isReceivedConfirmationByte = false;
                countDownRepeater();
                isDisabled = false;
            }
        }

        @RequiresApi(api = Build.VERSION_CODES.KITKAT)
        @Override
        public void onFinish() {
            try {
                if (packetNumber < totalpackets) {
                    MainActivity.serialPort.write(sendTextBytes[packetNumber]);
                    Toast.makeText(context, "Sent Packet " + (packetNumber + 1) + "/" + sendTextBytes.length +" try " + (repTimer+1), Toast.LENGTH_SHORT).show();
                    packetNumber++;
                    resendTimer.cancel();
                    resendCanceller.cancel();
                    resendTimer.start();
                    resendCanceller.start();
                } else {
                    packetNumber = 0;
                    isDisabled = false;
                    resendTimer.cancel();
                    resendCanceller.cancel();
                }
                if (packetNumber == totalpackets) {
                    countDownRepeater();
                    packetNumber = 0;
                }
            }catch (Exception e){
                Toast.makeText(context, "Please connect EMA device!", Toast.LENGTH_SHORT).show();
                isDisabled = false;
                packetNumber = 0;
                resendTimer.cancel();
                resendCanceller.cancel();
            }


        }
    };

    private void countDownRepeater(){
        if (repTimer == 4){
            resendTimer.cancel();
            resendCanceller.cancel();
            isDisabled = false;
            repTimer = 0;
            packetNumber = 0;
            Toast.makeText(context, "Successfully sent message to "
                    + RID, Toast.LENGTH_SHORT).show();

            //if message is longer than 44 characters, add a function here to send the next packet.
        }else if(repTimer < 3){
            repTimer++;
            resendTimer.cancel();
            resendCanceller.cancel();
            resendTimer.start();
            resendCanceller.start();
        }else if (repTimer == 3){
            // if needed, add a notification part here
            isDisabled = false;
            resendCanceller.cancel();
            resendTimer.cancel();
            repTimer = 0;
            Toast.makeText(context, "Failed to send message to " + RID, Toast.LENGTH_SHORT).show();
        }else{
            repTimer = 0;
            isDisabled = false;
            resendCanceller.cancel();
            resendTimer.cancel(); // for error trapping (stop the loop)
        }
    }

    @Override
    public View onCreateView(LayoutInflater inflater, ViewGroup container,
                             Bundle savedInstanceState) {
        // Inflate the layout for this fragment
        return inflater.inflate(R.layout.fragment_text_message, container, false);
    }

    @RequiresApi(api = Build.VERSION_CODES.KITKAT)
    @Override
    public void onViewCreated(@NonNull View view, @Nullable @org.jetbrains.annotations.Nullable Bundle savedInstanceState) {
        super.onViewCreated(view, savedInstanceState);
        this.context = getActivity();

        //textView = getActivity().findViewById(R.id.main_serialMonitor);
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
        sendButton.setOnClickListener(v -> onClickSendButton(v));

    }


    // This function retrieves the contact number (SID) of the user.
    void getSID(){
        Cursor cursor = dataBaseHelper.readUserSID();
        if(cursor.getCount() < 1){
            Toast.makeText(context, "Error Getting SID", Toast.LENGTH_SHORT).show();
        }else{
            if(cursor.moveToFirst()){
                    SID = cursor.getString(0);  //Names
                //tvAppend(textView, SID+"\n");
            }
        }

        cursor.close();
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
            //tvAppend(textView, "\nName:" + number.getSelectedItem().toString() + RID);
        }

        cursor.close();
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

        cursor.close();
    }

    /*private void tvAppend(TextView tv, CharSequence text) {
        final TextView ftv = tv;
        final CharSequence ftext = text;

        getActivity().runOnUiThread(() -> ftv.append(ftext));
    }

     */

    String getUserSID(){
        String SID = null;
        Cursor cursor;
        cursor = dataBaseHelper.readUserSID();

        if (cursor.getCount() == 0) {
            Toast.makeText(context, "No User SID!", Toast.LENGTH_SHORT).show();
        } else {
            if(cursor.moveToFirst()){
                SID = cursor.getString(0);     //CONTACT NUM
                while(cursor.moveToNext())
                    SID = cursor.getString(0);     //CONTACT NUM
            }
        }

        cursor.close();
        return SID;
    }
}