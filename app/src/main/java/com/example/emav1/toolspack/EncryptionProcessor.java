package com.example.emav1.toolspack;


import android.os.Bundle;
import android.util.Base64;
import android.widget.TextView;
import android.widget.Toast;

import androidx.appcompat.app.AppCompatActivity;
import org.bouncycastle.crypto.CryptoException;
import org.bouncycastle.crypto.engines.TwofishEngine;
import org.bouncycastle.crypto.modes.CBCBlockCipher;
import org.bouncycastle.crypto.paddings.PaddedBufferedBlockCipher;
import org.bouncycastle.crypto.params.KeyParameter;

import java.nio.charset.StandardCharsets;
import java.util.Arrays;


public class EncryptionProcessor {
    private String plainText = "Among Us (coloquially termed \"amogus\") teaches us to punish the minority and hate those who are different and unique as \"impostors.\" Instead, I like ";
    private String key       = "0908812959009085562143";
    int packetTotal;
    private byte[] cipherText, receivedCipherText;
    private byte[][] dividedCipherText;

  /*  @Override
    protected void onCreate(Bundle savedInstanceState) {
        cipherText = performEncrypt(key.getBytes(StandardCharsets.UTF_8), plainText);
        packetTotal =  (int)Math.ceil((double)cipherText.length / 40);
        //Divides the whole cipherText into a dividedCipherText[x][40] if exceeds 40byte limit, else is saved in dividedCipherText[0][0]
        dividedCipherText = splitCipherForPacket(cipherText, packetTotal);
        //after this point, we can send dividedCipherText[][] sa packet

        String senderSideTextString = Base64.encodeToString(cipherText, Base64.DEFAULT); //test var, unused in real application


        //Receiver part, putting together the divided Strings:
        receivedCipherText = concatenatedCipher(dividedCipherText, 10);
        String cipherTextString = Base64.encodeToString(receivedCipherText, Base64.DEFAULT);

        outputTextBox.setText(Arrays.toString(cipherText));
        encryptedTextBox.setText(Arrays.toString(receivedCipherText));


        //Decryption part
        String decryptText = performDecrypt(key.getBytes(StandardCharsets.UTF_8), receivedCipherText);

        //Some checks if equal ba, note that the ciphers don't necessarily have to be equal....I think
        boolean senderReceiverCiphersMatch = receivedCipherText.equals(cipherText) ? true : false;
        boolean senderReceiverStringsMatch = decryptText.equals(plainText);


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
            Toast.makeText(this, "Unexpected Encrypt exception has occurred.", Toast.LENGTH_SHORT).show();
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
            Toast.makeText(this, "Unexpected Decrypt exception has occurred.", Toast.LENGTH_SHORT).show();
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
            //can probably dispose of cipherTextOriginalBlock here to clean memory pero idk how that works and dont care AHAHAH
        }
        else {
            finalCipherBlock[0] = cipherTextOriginalBlock;
        }
        return finalCipherBlock;
    }
    private byte[] concatenatedCipher(byte[][] receivedCipher, int maxAllowablePackets){
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

   */
}
