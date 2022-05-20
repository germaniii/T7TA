package com.example.emav1.toolspack;
import java.math.BigDecimal;
import java.math.BigInteger;
import java.util.Date;
import java.util.Random;

public class DHProtocol {

    private BigInteger PUBLIC_KEY;
    private BigInteger RECEIVED_PUBLIC_KEY;
    private int PRIVATE_KEY;

    private BigInteger CIPHER_KEY;

    //An example 396-Digit Prime number
    private final BigInteger p = new BigInteger("315791951375393537137595555337555955191395351995195751755791151795317131135377351919777977373317997317733397199751739199735799971153399111973979977771537137371797357935195531355957399953977139577337393111951779135151171355371173379337573915193973715113971779315731713793579595533511197399993313719939759551175175337795317333957313779755351991151933337157555517575773115995775199513553337335137111");
    private final int g = 5; // Primitive rood modulo of p

    public DHProtocol(int PRIVATE_KEY) {
        this.PRIVATE_KEY = PRIVATE_KEY;
        this.PUBLIC_KEY = moduloResult(0);
    }

    public DHProtocol(BigInteger PUBLIC_KEY,int PRIVATE_KEY) {
        this.PRIVATE_KEY = PRIVATE_KEY;
        this.PUBLIC_KEY = PUBLIC_KEY;
        this.CIPHER_KEY = moduloResult(1);
    }
/*
    public void uponPublicKeyReceived(BigInteger RECEIVED_PUBLIC_KEY) {
        this.RECEIVED_PUBLIC_KEY = RECEIVED_PUBLIC_KEY;
    }

 */

    private BigInteger moduloResult (int operation){
        switch(operation){
            case 0: //previously "A" or "B"
                return new BigInteger(String.valueOf(this.g)).pow(this.PRIVATE_KEY).remainder(p);
            case 1: //"Key" For the Sender-side key
                return new BigInteger(String.valueOf(this.PUBLIC_KEY)).pow(this.PRIVATE_KEY).remainder(p);
            default:
                return BigInteger.ZERO;
        }
    }

    public BigInteger getCipherKey() {
        return this.CIPHER_KEY;
    }
    //TEST VARIABLE ONLY. TO-DO: REMOVE AFTER HANDSHAKE PROTOCOL IS ACCOMPLISHED.
    public BigInteger getPublicKey() {
        return this.PUBLIC_KEY;
    }

    /*
    public BigInteger getReceivedPublicKey() {
        return this.RECEIVED_PUBLIC_KEY;
    }

     */
    public int getPrivateKey() {
        return this.PRIVATE_KEY;
    }
}