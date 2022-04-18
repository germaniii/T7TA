package com.example.emav1.toolspack;
import java.math.BigDecimal;
import java.util.Date;

public class DHProtocol {

    public int SID;
    public int RID;

    private BigDecimal A;
    private BigDecimal B;
    private int a; //secretNumberA;
    private int b; //secretNumberB;

    private BigDecimal key;
    private BigDecimal otherKey;

    private final int g = 5; // Primitive rood modulo of p
    //private final int p = 23; // Public Prime Number

    //A 396-Digit Prime number
    private final BigDecimal p = new BigDecimal("315791951375393537137595555337555955191395351995195751755791151795317131135377351919777977373317997317733397199751739199735799971153399111973979977771537137371797357935195531355957399953977139577337393111951779135151171355371173379337573915193973715113971779315731713793579595533511197399993313719939759551175175337795317333957313779755351991151933337157555517575773115995775199513553337335137111");

    private long totalTimeElapsed;

    public DHProtocol(int SID, int RID) {
        this.SID = SID;
        this.RID = RID;
    }

    public void generateSecrets() {
        this.a = this.SID;
        this.b = this.RID;
        this.A = doMagic('A');
        this.B = doMagic('B');
        long timeBegin = new Date().getTime();
        this.key = doMagic('K');
        this.otherKey = doMagic('O');
        this.totalTimeElapsed = new Date().getTime() - timeBegin;
    }

    private BigDecimal doMagic (char whichSide){
        switch(whichSide){
            case 'A':
                return new BigDecimal(this.g ^ this.a).remainder(p);
            case 'B':
                return new BigDecimal(this.g ^ this.b).remainder(p);
            case 'K': //"Key" For the Sender-side key
                return this.B.pow(this.a).remainder(p);
            case 'O': // "Other" For the Receiver-side key
                return this.A.pow(this.b).remainder(p);
            default:
                return BigDecimal.ZERO;
        }
    }

    private static BigDecimal bigDecimalExponent (BigDecimal number, BigDecimal exponent){
        BigDecimal  base = number;
        for (BigDecimal counter = BigDecimal.ZERO ; counter.compareTo(exponent) < 0 ; counter = counter.add(BigDecimal.ONE)) {
            number = number.multiply(base);
        }
        return number;
    }

    @Override
    public String toString() {
        return "{ A='" + getA() + ", B='" + getB() +
                ", a='" + geta() + ", b='" + getb() + "'\n" +
                "key='" + getKey() + "'\n\n" +
                "(other key)='" + getOtherKey() +"'\n" +
                "}\n\n";
    }

    public long getTotalTimeElapsed() {
        return this.totalTimeElapsed;
    }

    public BigDecimal getKey() {
        return this.key;
    }

    public BigDecimal getOtherKey() {
        return this.otherKey;
    }

    public BigDecimal getA() {
        return this.A;
    }

    public int geta() {
        return this.a;
    }

    public void setA(BigDecimal A) {
        this.A = A;
    }

    public BigDecimal getB() {
        return this.B;
    }

    public int getb() {
        return this.b;
    }

    public void setB(BigDecimal B) {
        this.B = B;
    }

    public int getG() {
        return this.g;
    }

    public BigDecimal getP() {
        return this.p;
    }
}