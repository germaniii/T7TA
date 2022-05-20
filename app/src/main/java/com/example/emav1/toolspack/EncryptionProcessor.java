package com.example.emav1.toolspack;

import android.os.Build;
import android.util.Log;

import androidx.annotation.RequiresApi;

import org.bouncycastle.crypto.CryptoException;
import org.bouncycastle.crypto.engines.TwofishEngine;
import org.bouncycastle.crypto.modes.CBCBlockCipher;
import org.bouncycastle.crypto.paddings.PaddedBufferedBlockCipher;
import org.bouncycastle.crypto.params.KeyParameter;

import java.math.BigDecimal;
import java.math.BigInteger;
import java.nio.charset.StandardCharsets;
import java.security.spec.EncodedKeySpec;
import java.util.Arrays;



public class EncryptionProcessor {

    //Variables for Sender operations
    private byte[] cipherText;
    private byte[][] dividedCipherText;

    //Variables for Receiver operations
    private String decodedText;

    //Variables for all operations
    private String key;
    int packetTotal;

    //

    public EncryptionProcessor(){
        //none
    }

    //Call this for encrypting purposes -- Sender device
    @RequiresApi(api = Build.VERSION_CODES.KITKAT)
    public void sendingEncryptionProcessor(String inputText, BigInteger PUBLICKEY, int PRIVATEKEY){
        //this.key = generateKey(senderID, receiverID, true);
        this.key = new DHProtocol(PUBLICKEY, PRIVATEKEY).getCipherKey().toString().substring(0,32);
        Log.d("DHKeys", "Public Key : " + PUBLICKEY.toString());
        Log.d("DHKeys", "Private Key : " + PRIVATEKEY);
        Log.d("DHKeys", "Cipher Key : " + this.key);
        this.cipherText = performEncrypt(this.key.getBytes(StandardCharsets.UTF_8), inputText);
    }

    //Call for decrypting -- Receiver device
    @RequiresApi(api = Build.VERSION_CODES.KITKAT)
    public void receivingEncryptionProcessor(byte[] receivedCipherText, BigInteger PUBLICKEY, int PRIVATEKEY){
        //this.key = generateKey(senderID, receiverID, false);
        this.key = new DHProtocol(PUBLICKEY, PRIVATEKEY).getCipherKey().toString().substring(0,32);
        Log.d("DHKeys", "Public Key : " + PUBLICKEY.toString());
        Log.d("DHKeys", "Private Key : " + PRIVATEKEY);
        Log.d("DHKeys", "Cipher Key : " + this.key);
        this.decodedText = performDecrypt(this.key.getBytes(StandardCharsets.UTF_8), receivedCipherText);
    }

    public byte[] getCipherText() {
        return cipherText;
    }


    public String getDecodedText(){
        return this.decodedText;
    }


    public String generateKey(String senderID, String receiverID, boolean isSending) {
        /* This is the old keyGenerator
        StringBuilder sID, rID;
        sID = new StringBuilder(senderID);
        rID = new StringBuilder(receiverID);
        return sID.replace(0, 2, "").toString() + rID.replace(0, 2, "").toString();
         */
        //Swapping for sending and receiving
        int person1, person2;
        if(isSending){
            person1 = Integer.parseInt(senderID.substring(2,5));
            person2 = Integer.parseInt(receiverID.substring(7,10));
        }else{
            person1 = Integer.parseInt(receiverID.substring(7,10));
            person2 = Integer.parseInt(senderID.substring(2,5));
        }

        if(isSending)
            //key = dhalgo.getKey().toString();

        key.replace('0','1');
        if(key.length() < 32){
            for(int i = key.length(); i < 32; i++)
                key += "1";
        }
       // Log.d("ADebugTag", "getKey: " + dhalgo.getKey().toString().substring(0,32));
       // Log.d("ADebugTag", "getOtherKey: " + dhalgo.getOtherKey().toString().substring(0,32));

        return key.substring(0,32);

    }

    private byte[] performEncrypt(byte[] key, String plainText)
    {
        byte[] ptBytes = plainText.getBytes();

        PaddedBufferedBlockCipher cipher = new PaddedBufferedBlockCipher(
                new CBCBlockCipher(new TwofishEngine()));

        cipher.init(true, new KeyParameter(key));

        byte[] rv = new byte[cipher.getOutputSize(ptBytes.length)];

        int oLen = cipher.processBytes(ptBytes, 0, ptBytes.length, rv, 0);
        try
        {
            cipher.doFinal(rv, oLen);
        }
        catch (CryptoException ce)
        {
            //     Toast.makeText(this , "Unexpected Encrypt exception has occurred.", Toast.LENGTH_SHORT).show();
        }
        return rv;
    }

    private String performDecrypt(byte[] key, byte[] cipherText)
    {
        PaddedBufferedBlockCipher cipher = new PaddedBufferedBlockCipher(
                new CBCBlockCipher(new TwofishEngine()));

        cipher.init(false, new KeyParameter(key));

        byte[] rv = new byte[cipher.getOutputSize(cipherText.length)];

        int oLen = cipher.processBytes(cipherText, 0, cipherText.length, rv, 0);
        try
        {
            cipher.doFinal(rv, oLen);
        }
        catch (CryptoException ce)
        {
            //    Toast.makeText(this, "Unexpected Decrypt exception has occurred.", Toast.LENGTH_SHORT).show();
        }
        return new String(rv).trim();
    }

    private byte[][] splitCipherForPacket(byte[] cipherTextOriginalBlock, int numOfPackets){

        byte[][] finalCipherBlock = new byte[numOfPackets][40];
        if (numOfPackets > 1) {
            int fromIndex = 0;
            int toIndex = fromIndex + 40;
            int numofpadding=0;
            int numofnonzero=0;

            for (int i = 0; i < numOfPackets; i++) {
                if(i == numOfPackets-1){
                    for(int j = 0; j < cipherTextOriginalBlock.length; j++){
                        if(cipherTextOriginalBlock[j] == 0)
                            numofpadding+=1;
                        else
                            numofnonzero+=1;
                    }

                    for(int j = 0; j<numofpadding; j++){
                        cipherTextOriginalBlock[j+numofnonzero] = 0x2E;
                    }

                }
                finalCipherBlock[i] = Arrays.copyOfRange(cipherTextOriginalBlock, fromIndex, toIndex); //since 40b per packet
                fromIndex = toIndex;
                toIndex = fromIndex + 40;
            }
        }
        else {
            finalCipherBlock[0] = cipherTextOriginalBlock;
        }
        return finalCipherBlock;
    }

}
