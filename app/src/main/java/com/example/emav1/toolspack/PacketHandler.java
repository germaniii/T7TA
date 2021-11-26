package com.example.emav1.toolspack;

import java.nio.ByteBuffer;

public class PacketHandler {

    static int packetNumberLimit = 5;
    static byte packetEOF = 0x7F; //For SMP byte purposes, 0x7F is signed byte limit.

    int numOfPackets;
    byte[] SID;
    byte[] RID;
    byte[][] messageCipher;
    byte[][] packetHash;

    byte[][] receivedData;

    public PacketHandler(){
    }

    public void setSID(String sSID){
        this.SID = formatID(sSID);
    }

    public byte[] getSIDBytes(){
        return this.SID;
    }

    //constructor for sending operations, call for assembling packets post-encryption
    //args: SID and RID     = must be 4B each
    //      messageCipher   = must be 60B
    //      packetHash      = must be 11B
    public void setSendParameters(String SID, String RID, byte[][] messageCipher, byte[][] packetHash){
        this.SID = formatID(SID);
        this.RID = formatID(RID);
        this.messageCipher = messageCipher;
        this.packetHash = packetHash;
        this.numOfPackets= messageCipher.length;
    }
    //constructor for receiving operations, call for disassembling packets pre-decryption
    //args: receivedData = must be 60B, input stream from ArduinoNano
    public void setRecvParameters(byte[][] receivedData){
        this.receivedData = receivedData;
        this.numOfPackets = receivedData.length;
        this.SID = new byte[4];
        this.RID = new byte[4];
        this.messageCipher = new byte[this.numOfPackets][40];
        this.packetHash = new byte[this.numOfPackets][11];
    }

    //=========================== SENDER METHOD/S

    // Copies all byte[] info provided in constructor, into an array of N packets of 60 bytes each.
    //Iterate through FIRST DIMENSION when accessing for sending
    // e.g. Something like:
    //      for (i = 0 ; o < numOfPackets ; i++)
    //          SendToArduinoFunction(getPacketsForSending[i]);
    //
    public byte[][] getPacketsForSending(){
        byte[] SMP = new byte[numOfPackets];
        byte SMPvar = 0x00;
        byte[][] packetsForSending = new byte[this.numOfPackets][60];

        for (int i = 0 ; i < this.numOfPackets - 1 ; i++, SMPvar++)
            SMP[i] = SMPvar;

        SMP[numOfPackets - 1] = packetEOF;
        for (int i = 0 ; i < this.numOfPackets ; i++){
            //args: Source array, index to start from in source array, destination array, index to start from in dest array, length of copy (e.g. x[0] to x[4] = 5)
            System.arraycopy(SMP, i, packetsForSending[i], 0, 1);
            System.arraycopy(this.SID, 0, packetsForSending[i], 1, 4);
            System.arraycopy(this.RID, 0, packetsForSending[i], 5, 4);
            System.arraycopy(this.messageCipher[i], 0, packetsForSending[i], 9, 40);
            System.arraycopy(this.packetHash[i], 0, packetsForSending[i], 49, 11);
        }
        return packetsForSending;
    }

    private byte[] formatID(String ID){
        String reducedID = new StringBuilder(ID).replace(0, 2, "").toString();
        return ByteBuffer.allocate(4).putInt(Integer.parseInt(reducedID)).array();
    }

    //=========================== RECEIVER METHOD/S
    //Assumes all from the same SID
    public void disassemblePackets(){
        //args: Source array, index to start from in source array, destination array, index to start from in dest array, length of copy (e.g. x[0] to x[4] = 5)
        System.arraycopy(this.receivedData[0], 1, this.SID, 0, 4);
        System.arraycopy(this.receivedData[0], 5, this.RID, 0, 4);
        for (int i = 0 ; i < this.numOfPackets; i++) {
            System.arraycopy(this.receivedData[i], 9, this.messageCipher[i], 0, 40);
            System.arraycopy(this.receivedData[i], 49, this.packetHash[i], 0, 11);
        }
    }

    //============================ GETTERS
    public String getID(byte[] inputByte){
        int intInput = ByteBuffer.wrap(inputByte).getInt();
        String input = String.valueOf(intInput);
        String padding = "";

        if (input.length() < 9){
            for (int i = input.length() ; i < 9 ; i++)
                padding += "0";
        }
        return "09" + padding + input;
    }

    public String getSenderID(){
        return getID(this.SID);
    }

    public String getReceiverID(){
        return getID(this.RID);
    }

    public String toString(boolean forSending){
        return "<<SID: [" + getID(this.SID) + "], RID: [" + getID(this.RID) + "]>>";
    }

    public int getNumOfPackets(){
        return this.numOfPackets;
    }
}

