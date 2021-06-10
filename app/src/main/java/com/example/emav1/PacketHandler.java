package com.example.emav1;

public class PacketHandler {

    String processData;

    byte SMP;
    short RID, SID;
    String ED,HK;

    PacketHandler(){}

    void PacketProcessor(byte[] dataset){

    }

    void assignProcessData(){
    }

    public String getProcessData() {
        return processData;
    }

    public void setProcessData(String processData) {
        this.processData = processData;
    }


    public byte getSMP() {
        return SMP;
    }

    public void setSMP(byte SMP) {
        this.SMP = SMP;
    }

    public String getED() {
        return ED;
    }

    public void setED(String ED) {
        this.ED = ED;
    }
}
