package com.example.emav1.toolspack;

import org.bouncycastle.crypto.CryptoException;
import org.bouncycastle.crypto.engines.TwofishEngine;
import org.bouncycastle.crypto.modes.CBCBlockCipher;
import org.bouncycastle.crypto.paddings.PaddedBufferedBlockCipher;
import org.bouncycastle.crypto.params.KeyParameter;

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

    public EncryptionProcessor(){
        //none
    }

    //Call this for encrypting purposes -- Sender device
    public void sendingEncryptionProcessor(String inputText, String senderID, String receiverID){
        this.key = generateKey(senderID, receiverID);
        this.cipherText = performEncrypt(this.key.getBytes(StandardCharsets.UTF_8), inputText);
        this.packetTotal = (int)Math.ceil((double)this.cipherText.length / 40);
        this.dividedCipherText = splitCipherForPacket(this.cipherText, this.packetTotal);
    }

    //Call for decrypting -- Receiver device
    public void receivingEncryptionProcessor(byte[][] receivedCipherText, String senderID, String receiverID){
        this.key = generateKey(senderID, receiverID);
        byte[] receivedCipherTextConcat = concatenatedCipher(receivedCipherText);
        this.decodedText = performDecrypt(this.key.getBytes(StandardCharsets.UTF_8), receivedCipherTextConcat);
    }

    public byte[] getCipherText() {
        return cipherText;
    }

    public byte[][] getDividedCipherText() {
        return dividedCipherText;
    }

    public String getDecodedText(){
        return this.decodedText;
    }

    public int getPacketTotal(){
        return this.packetTotal;
    }

    private String generateKey(String senderID, String receiverID) {
        StringBuilder sID, rID;
        sID = new StringBuilder(senderID);
        rID = new StringBuilder(receiverID);
        return sID.replace(0, 2, "").toString() + rID.replace(0, 2, "").toString();
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

            for (int i = 0; i < numOfPackets; i++) {
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


    private byte[] concatenatedCipher(byte[][] receivedCipher){
        if (receivedCipher.length > 1){
            int numOfPackets, numOfElements, padding = 0;
            numOfPackets = receivedCipher.length;
            numOfElements = numOfPackets * 40;

            byte[] paddedCipher = new byte[numOfElements];
            int lastIndexPos = 0;
            for (int i = 0; i < numOfPackets ; i++){
                System.arraycopy(receivedCipher[i], 0, paddedCipher, lastIndexPos, 40);
                lastIndexPos += 40;
            }
            for (int i = paddedCipher.length-1; i > 1 && paddedCipher[i] == 0 ; i--, padding++);
            byte[] removedZeroPadding = new byte[numOfElements - padding];
            System.arraycopy(paddedCipher, 0, removedZeroPadding, 0, numOfElements - padding);
            return removedZeroPadding;
        }
        return receivedCipher[0];
    }
}
