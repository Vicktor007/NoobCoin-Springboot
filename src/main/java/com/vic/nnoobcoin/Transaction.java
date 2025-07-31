package com.vic.nnoobcoin;



import com.vic.nnoobcoin.utility.StringUtil;

import java.security.PrivateKey;
import java.security.PublicKey;
import java.util.ArrayList;

public class Transaction {

    public String transactionID;
    public PublicKey senderAddress;
    public PublicKey recipientAddress;
    public float value;
    public byte[] signature; // to prevent anyone else from spending our wallet funds

    public ArrayList<TransactionInput> inputs = new ArrayList<TransactionInput>();
    public ArrayList<TransactionOutput> outputs = new ArrayList<TransactionOutput>();

    private static int sequence = 0; // a rough count of how many transactions have been generated

    public Transaction(PublicKey senderAddress, PublicKey recipientAddress, float value, ArrayList<TransactionInput> inputs){
        this.senderAddress = senderAddress;
        this.recipientAddress = recipientAddress;
        this.value = value;
        this.inputs = inputs;
    }

    // this calculates the transaction hash (which will be used as its id)
    private String getTransactionID(){
        sequence++; // increase the sequence to avoid 2 identical transactions having the same hash
        return StringUtil.applySha256(
                StringUtil.getStringFromKey(senderAddress) +
                        StringUtil.getStringFromKey(recipientAddress) +
                        Float.toString(value) + sequence
        );
    }

    // signs all the data we dont wish to be tampered with
    public  void generateSignature(PrivateKey privateKey) {
        String data = StringUtil.getStringFromKey(senderAddress) + StringUtil.getStringFromKey(recipientAddress) + Float.toString(value);
        signature = StringUtil.applyECDSASig(privateKey, data);
    }

    // verifies the data we signed hasnt been tampered with
    public boolean verifySignature() {
        String data = StringUtil.getStringFromKey(senderAddress) + StringUtil.getStringFromKey(recipientAddress) + Float.toString(value);
        return StringUtil.verifyECDSASig(senderAddress, data, signature);
    }

    public boolean processTransaction() {
        if (!verifySignature()) {
            System.out.println("Transaction Signature verification failed");
            return false;
        }

        // gather transaction inputs that are unspent
        for (TransactionInput input : inputs) {
            input.UTXO = NnoobcoinApplication.UTXOS.get(input.transactionOutputID);
        }

        // check if transaction is vaild:
        if (getInputsValue() < NnoobcoinApplication.minimumTransaction){
            System.out.println("Transaction input value too low: " + getInputsValue());
            return false;
        }

        // generate transaction outputs:
        float leftOver = getInputsValue() - value; // get the inputs value then the leftover change
        transactionID = getTransactionID();
        outputs.add(new TransactionOutput(this.recipientAddress, value, transactionID)); // send value to recipient

        outputs.add(new TransactionOutput(this.senderAddress, leftOver, transactionID)); // send the leftover change back to sender

        //add outputs to unspent list
        for (TransactionOutput output : outputs) {
            NnoobcoinApplication.UTXOS.put(output.id , output);
        }

        // remove transaction inputs from UTXO lists as spent
        for (TransactionInput input : inputs) {
            if (input.UTXO == null) continue; //skip transaction if it cant be found
            NnoobcoinApplication.UTXOS.remove(input.UTXO.id);
        }
        return true;
    }
// returns sum of inputs(UTXOs) values

    public float getInputsValue(){
        float total = 0;
        for (TransactionInput input : inputs) {
            if (input.UTXO == null) continue; // if transaction cant be found skip it
            total += input.UTXO.value;
        }
        return total;
    }

    // returns sum of outputs
    public float getOutputsValue(){
        float total = 0;
        for (TransactionOutput output : outputs) {
            total += output.value;
        }
        return total;
    }

}
