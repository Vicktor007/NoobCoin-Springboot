package com.vic.nnoobcoin;



import com.vic.nnoobcoin.utility.StringUtil;

import java.security.PublicKey;

public class TransactionOutput {
    public String id;
    public PublicKey recipient; //also known as the new owner of these coins
    public float value; // the amount of coins they own
    public String parentTransactionID; // the id of the transaction this output was created in

    public TransactionOutput(PublicKey recipient, float value, String parentTransactionID) {
        this.recipient = recipient;
        this.value = value;
        this.parentTransactionID = parentTransactionID;
        this.id = StringUtil.applySha256(StringUtil.getStringFromKey(recipient) + Float.toString(value) +  parentTransactionID);
    }

    // Check if coin belongs to you
    public boolean coinIsMine(PublicKey publicKey) {
        return  publicKey.equals(recipient);
    }

}
