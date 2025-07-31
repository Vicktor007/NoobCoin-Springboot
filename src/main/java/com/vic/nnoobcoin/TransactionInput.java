package com.vic.nnoobcoin;

public class TransactionInput {
    public String transactionOutputID; //reference to transactionOutputs -> transactionId
    public TransactionOutput UTXO; //contains the unspent transaction output

    public TransactionInput(String transactionOutputID) {
        this.transactionOutputID = transactionOutputID;
    }
}
