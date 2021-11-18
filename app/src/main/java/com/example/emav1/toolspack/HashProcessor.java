package com.example.emav1.toolspack;


import java.io.UnsupportedEncodingException;
import java.nio.charset.StandardCharsets;
import java.security.MessageDigest;
import java.security.NoSuchAlgorithmException;

public class HashProcessor {
    public String obtainSHA(String s) throws NoSuchAlgorithmException, UnsupportedEncodingException {
        // Static getInstance() method is invoked with the hashing SHA-256
        MessageDigest sha = MessageDigest.getInstance("SHA-256");
        sha.update(s.getBytes("UTF-8"));

        // the digest() method is invoked
        // to compute the message digest of the input
        // and returns an array of byte
        return sha.digest().toString();

        //TO BE FIXED, THE OUTPUT IS DIFFERENT EACH TIME. SOMETHING TO DO WITH THE ENCODING.
    }
}
