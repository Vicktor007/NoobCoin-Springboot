package com.vic.nnoobcoin.utility;


import com.vic.nnoobcoin.Transaction;

import java.nio.charset.StandardCharsets;
import java.security.MessageDigest;
import java.security.*;
import java.security.spec.PKCS8EncodedKeySpec;
import java.security.spec.X509EncodedKeySpec;
import java.util.ArrayList;
import java.util.Base64;

public class StringUtil {
//    Applies Sha@56 to a string and returns the result

    public static String applySha256(String input){
        try{
            MessageDigest digest = MessageDigest.getInstance("SHA-256");
//            Applies sha256 to our input
            byte[] hash = digest.digest(input.getBytes(StandardCharsets.UTF_8));
            StringBuilder hexString = new StringBuilder(); // This will contain hash as hexadecimal
            for (byte b : hash) {
                String hex = Integer.toHexString(0xff & b);
                if (hex.length() == 1) hexString.append('0');
                hexString.append(hex);
            }
            return hexString.toString();
        } catch(Exception e){
            throw new RuntimeException(e);
        }
    }

    //    applies ECDSA signature and returns the result (as bytes)
    public static byte[] applyECDSASig(PrivateKey privateKey, String input){
        Signature dsa;
        byte[] output = new byte[0];
        try{
            dsa = Signature.getInstance("ECDSA","BC");
            dsa.initSign(privateKey);
            byte[] strByte = input.getBytes();
            dsa.update(strByte);
            output = dsa.sign();
        } catch(Exception e){
            throw new RuntimeException(e);
        }
        return output;
    }

    // Verifies aString signature
    public static boolean verifyECDSASig(PublicKey publicKey, String data, byte[] signature){
        try{
            Signature ecdsaVerify = Signature.getInstance("ECDSA", "BC");
            ecdsaVerify.initVerify(publicKey);
            ecdsaVerify.update(data.getBytes());
            return ecdsaVerify.verify(signature);
        } catch(Exception e){
            throw new RuntimeException(e);
        }
    }

    public static String getStringFromKey(Key key){
        return Base64.getEncoder().encodeToString(key.getEncoded());
    }


        public static PrivateKey getPrivateKeyFromString(String key) {
            try {
                byte[] keyBytes = Base64.getDecoder().decode(key);
                PKCS8EncodedKeySpec keySpec = new PKCS8EncodedKeySpec(keyBytes);
                KeyFactory keyFactory = KeyFactory.getInstance("EC"); // or "ECDSA"
                return keyFactory.generatePrivate(keySpec);
            } catch (Exception e) {
                throw new RuntimeException("Failed to convert string to PrivateKey", e);
            }
        }

    public static PublicKey getPublicKeyFromString(String key) {
        try {
            byte[] keyBytes = Base64.getDecoder().decode(key);
            X509EncodedKeySpec keySpec = new X509EncodedKeySpec(keyBytes);
            KeyFactory keyFactory = KeyFactory.getInstance("EC"); // or "ECDSA"
            return keyFactory.generatePublic(keySpec);
        } catch (Exception e) {
            throw new RuntimeException("Failed to convert string to PublicKey", e);
        }
    }

    public static byte[] getBytesFromSignatureString(String signatureString) {
        return Base64.getDecoder().decode(signatureString);
    }




//    Tacks in array of transactions and returns a merkle root

    public static String getMerkleRoot(ArrayList<Transaction> transactions){
        int count = transactions.size();
        ArrayList<String> previousTreeLayer = new ArrayList<String>();
        for (Transaction transaction : transactions){
            previousTreeLayer.add(transaction.transactionID);
        }
        ArrayList<String> treeLayer = previousTreeLayer;
        while (count > 1){
            treeLayer = new ArrayList<String>();
            for (int i = 1; i < previousTreeLayer.size(); i++){
                treeLayer.add(applySha256(previousTreeLayer.get(i-1) + previousTreeLayer.get(i)));
            }
            count = treeLayer.size();
            previousTreeLayer = treeLayer;
        }
        return (treeLayer.size() == 1) ? treeLayer.getFirst() : "";
    }

    //Returns difficulty string target, to compare to hash. eg difficulty of 5 will return "00000"
    public static String getDificultyString(int difficulty) {
        return new String(new char[difficulty]).replace('\0', '0');
    }
}
