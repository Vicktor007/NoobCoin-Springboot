package com.vic.nnoobcoin;

import java.security.*;
import java.security.spec.ECGenParameterSpec;
import java.util.ArrayList;
import java.util.HashMap;
import java.util.Map;

public class Wallet {
    public PrivateKey privateKey;
    public PublicKey publicKey;

    public HashMap<String, TransactionOutput> UTXOS = new HashMap<String,TransactionOutput>();

    public Wallet(){
        generateKeyPair();
    }
    //  this method  uses Java.security.KeyPairGenerator to generate an Elliptic Curve KeyPair. This methods makes and sets our Public and Private keys.
    public void generateKeyPair(){
        try{
            KeyPairGenerator keyPairGenerator = KeyPairGenerator.getInstance("ECDSA","BC");
            SecureRandom random = SecureRandom.getInstance("SHA1PRNG");
            ECGenParameterSpec ecGenParameterSpec = new ECGenParameterSpec("prime192v1");

//          Initialize the key generator and generate a keyPair
            keyPairGenerator.initialize(ecGenParameterSpec, random); // 256 bytes provides an acceptable security level
            KeyPair keyPair = keyPairGenerator.generateKeyPair();

//            // set the public and private keys from the keyPair
            privateKey = keyPair.getPrivate();
            publicKey = keyPair.getPublic();
        } catch(Exception e){
            throw  new RuntimeException(e);
        }
    }

//    returns balance and stores the UTXO's owned by this wallet in this.UTXOs
    public float getBalance(){
        float total = 0;
        for(Map.Entry<String, TransactionOutput> item : NnoobcoinApplication.UTXOS.entrySet()){
            TransactionOutput UTXO = item.getValue();
            if (
                    UTXO.coinIsMine(publicKey) // if coins belong to me
            ) {
                UTXOS.put(UTXO.id, UTXO); // add it to our list of unspent transactions
                total += UTXO.value;
            }
        }
        return total;
    }

    // generates and returns a new transaction from this wallet.
    public Transaction sendFunds(PublicKey _recipient, float value){
        if (getBalance() < value) // gather balance and check funds availability
            {
                System.out.println("Not enough funds to send this transaction. Transaction discarded.");
                return null;
        }
        // create array list of inputs
        ArrayList<TransactionInput> inputs = new ArrayList<TransactionInput>();

        float total = 0;
        for(Map.Entry<String, TransactionOutput> item : NnoobcoinApplication.UTXOS.entrySet()){
            TransactionOutput UTXO = item.getValue();
            total+=UTXO.value;
            inputs.add(new TransactionInput(UTXO.id));
            if (total > value) break;
        }

        Transaction newTransaction = new Transaction(publicKey, _recipient, value, inputs);
        newTransaction.generateSignature(privateKey);

        for (TransactionInput input : inputs) {
            UTXOS.remove(input.transactionOutputID);
        }
        return newTransaction;
    }
}
