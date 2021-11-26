package com.example.emav1.toolspack;

import android.os.Build;

import androidx.annotation.RequiresApi;

import java.nio.charset.StandardCharsets;
import android.util.Base64;

public class HashProcessor {
    private static final int[] roundconstant_zero = { 0x6, 0xa, 0x0, 0x9, 0xe, 0x6, 0x6, 0x7, 0xf, 0x3, 0xb, 0xc,
            0xc, 0x9, 0x0, 0x8, 0xb, 0x2, 0xf, 0xb, 0x1, 0x3, 0x6, 0x6, 0xe, 0xa, 0x9, 0x5, 0x7, 0xd, 0x3,
            0xe, 0x3, 0xa, 0xd, 0xe, 0xc, 0x1, 0x7, 0x5, 0x1, 0x2, 0x7, 0x7, 0x5, 0x0, 0x9, 0x9, 0xd, 0xa,
            0x2, 0xf, 0x5, 0x9, 0x0, 0xb, 0x0, 0x6, 0x6, 0x7, 0x3, 0x2, 0x2, 0xa };
    private static final int[][] S = { { 9, 0, 4, 11, 13, 12, 3, 15, 1, 10, 2, 6, 7, 5, 8, 14 },
            { 3, 12, 6, 13, 5, 7, 1, 9, 15, 2, 0, 4, 11, 10, 14, 8 } };

    private static int[] A = new int[256];
    private static int[] H;

    private static int[] buffer;
    private static int[] roundconstant = new int[64];
    private static long databitlen;

    private static void L(int i, int[] tem) {
        int a = tem[i];
        int b = tem[i + 1];

        (b) ^= (((a) << 1) ^ ((a) >> 3) ^ (((a) >> 2) & 2)) & 0xf;
        (a) ^= (((b) << 1) ^ ((b) >> 3) ^ (((b) >> 2) & 2)) & 0xf;

        tem[i] = a;
        tem[i + 1] = b;
    }

    public static void Final(byte[] hashval) {

        if ((databitlen & 0x1ff) == 0) {
            for (int i = 0; i < 64; i++)
                buffer[i] = 0;
            buffer[0] = 0x80;
            buffer[63] = (int) (databitlen & 0xff);
            buffer[62] = (int) ((databitlen >>> 8) & 0xff);
            buffer[61] = (int) ((databitlen >>> 16) & 0xff);
            buffer[60] = (int) ((databitlen >>> 24) & 0xff);
            buffer[59] = (int) ((databitlen >>> 32) & 0xff);
            buffer[58] = (int) ((databitlen >>> 40) & 0xff);
            buffer[57] = (int) ((databitlen >>> 48) & 0xff);
            buffer[56] = (int) ((databitlen >>> 56) & 0xff);
            F8();
        } else {
            buffer[(int) ((databitlen & 0x1ff) >> 3)] |= 1 << (7 - (databitlen & 7));
            F8();
            for (int k = 0; k < 64; k++)
                buffer[k] = 0;
            buffer[63] = (int) (databitlen & 0xff);
            buffer[62] = (int) ((databitlen >>> 8) & 0xff);
            buffer[61] = (int) ((databitlen >>> 16) & 0xff);
            buffer[60] = (int) ((databitlen >>> 24) & 0xff);
            buffer[59] = (int) ((databitlen >>> 32) & 0xff);
            buffer[58] = (int) ((databitlen >>> 40) & 0xff);
            buffer[57] = (int) ((databitlen >>> 48) & 0xff);
            buffer[56] = (int) ((databitlen >>> 56) & 0xff);
            F8();
        }

        for (int j = 0; j < 32; j++) {
            hashval[j] = (byte) H[j + 96];
        }

    }

    public static void Update(byte[] data, long databitle) {

        databitlen = databitle;

        for (int i = 0; (i + 512) <= databitlen; i = i + 512) {

            for (int j = 0; j < 64; j++)
                buffer[j] = data[j + (i >> 3)];
            F8();
        }

        if ((databitlen & 0x1ff) > 0) {
            for (int i = 0; i < 64; i++)
                buffer[i] = 0;

            if ((databitlen & 7) == 0) {
                for (int i = 0; i < (databitlen & 0x1ff) >>> 3; i++)
                    buffer[i] = data[(int) (i + ((databitlen >>> 9) << 6))];
            } else {

                for (int i = 0; i < (((databitlen & 0x1ff) >>> 3) + 1); i++) {
                    buffer[i] = data[(int) (i + ((databitlen >>> 9) << 6))];
                }
            }
        }
    }

    public static void Init() {

        buffer = new int[64];
        H = new int[128];

        H[1] = 256 & 0xff;
        H[0] = (256 >> 8) & 0xff;

        F8();

    }

    private static void F8() {

        for (int i = 0; i < 64; i++)
            roundconstant[i] = roundconstant_zero[i];

        for (int i = 0; i < 64; i++)
            H[i] ^= buffer[i];

        E8();

        for (int i = 0; i < 64; i++)
            H[i + 64] ^= buffer[i];

    }

    private static void E8() {

        int t0, t1, t2, t3;
        int[] tem = new int[256];

        for (int i = 0; i < 256; i++) {
            t0 = (H[i >> 3] >> (7 - (i & 7))) & 1;
            t1 = (H[(i + 256) >> 3] >> (7 - (i & 7))) & 1;
            t2 = (H[(i + 512) >> 3] >> (7 - (i & 7))) & 1;
            t3 = (H[(i + 768) >> 3] >> (7 - (i & 7))) & 1;
            tem[i] = (t0 << 3) | (t1 << 2) | (t2 << 1) | (t3 << 0);
        }

        for (int i = 0; i < 128; i++) {
            A[i << 1] = tem[i];
            A[(i << 1) + 1] = tem[i + 128];
        }

        for (int i = 0; i < 35; i++) {
            R8();
            update_roundconstant();
        }

        last_half_round_R8();

        for (int i = 0; i < 128; i++)
            H[i] = 0;

        for (int i = 0; i < 128; i++) {
            tem[i] = A[i << 1];
            tem[i + 128] = A[(i << 1) + 1];
        }

        for (int i = 0; i < 256; i++) {
            t0 = (tem[i] >> 3) & 1;
            t1 = (tem[i] >> 2) & 1;
            t2 = (tem[i] >> 1) & 1;
            t3 = (tem[i] >> 0) & 1;

            H[i >> 3] |= t0 << (7 - (i & 7));
            H[(i + 256) >> 3] |= t1 << (7 - (i & 7));
            H[(i + 512) >> 3] |= t2 << (7 - (i & 7));
            H[(i + 768) >> 3] |= t3 << (7 - (i & 7));
        }

    }

    private static void R8() {
        int[] tem = new int[256];
        int t;
        int[] roundconstant_expanded = new int[256];

        for (int i = 0; i < 256; i++) {
            roundconstant_expanded[i] = (roundconstant[i >> 2] >> (3 - (i & 3))) & 1;
        }

        for (int i = 0; i < 256; i++) {
            tem[i] = S[roundconstant_expanded[i]][A[i]];
        }

        for (int i = 0; i < 256; i = i + 2)
            L(i, tem);

        for (int i = 0; i < 256; i = i + 4) {
            t = tem[i + 2];
            tem[i + 2] = tem[i + 3];
            tem[i + 3] = t;
        }

        for (int i = 0; i < 128; i = i + 1) {
            A[i] = tem[i << 1];
            A[i + 128] = tem[(i << 1) + 1];
        }

        for (int i = 128; i < 256; i = i + 2) {
            t = A[i];
            A[i] = A[i + 1];
            A[i + 1] = t;
        }

    }

    private static void update_roundconstant() {

        int t;
        int[] tem = new int[64];

        for (int i = 0; i < 64; i++)
            tem[i] = S[0][roundconstant[i]];

        for (int i = 0; i < 64; i = i + 2)
            L(i, tem);

        for (int i = 0; i < 64; i = i + 4) {
            t = tem[i + 2];
            tem[i + 2] = tem[i + 3];
            tem[i + 3] = t;
        }

        for (int i = 0; i < 32; i = i + 1) {
            roundconstant[i] = tem[i << 1];
            roundconstant[i + 32] = tem[(i << 1) + 1];
        }

        for (int i = 32; i < 64; i = i + 2) {
            t = roundconstant[i];
            roundconstant[i] = roundconstant[i + 1];
            roundconstant[i + 1] = t;
        }
    }

    private static void last_half_round_R8() {

        int[] roundconstant_expanded = new int[256];

        for (int i = 0; i < 256; i++) {
            roundconstant_expanded[i] = (roundconstant[i >> 2] >> (3 - (i & 3))) & 1;
        }

        for (int i = 0; i < 256; i++) {
            A[i] = S[roundconstant_expanded[i]][A[i]];
        }
    }

    public static void Hash(byte[] data, long databitlen, byte[] hashval) {

        Init();
        Update(data, databitlen);
        Final(hashval);

    }

    @RequiresApi(api = Build.VERSION_CODES.KITKAT)
    public String getHash(String message){
        databitlen = 392; // 49bytes ( + SMP,SID,RID) = 392 bits
        byte[] hashval = new byte[256]; // fixed 256 size

        Hash(message.getBytes(), databitlen, hashval); // Perform Hash Function

        String convertedtoUTF8 = new String(hashval, StandardCharsets.UTF_8); // Convert hashval to string in UTF-8
        String convertedtoString = Base64.encodeToString(convertedtoUTF8.getBytes(), Base64.DEFAULT);

        return convertedtoString.substring(0,11);

    }

}
