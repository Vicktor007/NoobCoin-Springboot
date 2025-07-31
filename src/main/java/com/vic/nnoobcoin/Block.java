package com.vic.nnoobcoin;


import com.vic.nnoobcoin.utility.StringUtil;

import java.util.ArrayList;
import java.util.Date;
import java.util.Objects;

public class Block {

    public String hash;
    public String previousHash;
    public String merkleRoot;
    public ArrayList<Transaction> transactions =  new ArrayList<Transaction>(); // our data will be a simple message
    private long timestamp;
    private int nonce;

//    block constructor
    public Block(String previousHash) {

        this.previousHash = previousHash;
        this.timestamp =  new Date().getTime();
        this.hash = calculateHash();//Making sure we do this after we set the other values.
    }

//    calculate new hashes based on block contents
    public String calculateHash() {

         return StringUtil.applySha256(previousHash + Long.toString(timestamp) + Integer.toString(nonce) + merkleRoot);

    }

    //Increases nonce value until hash target is reached.
    public void mineBlock(int difficulty) {
        merkleRoot = StringUtil.getMerkleRoot(transactions);
        String target  = StringUtil.getDificultyString(difficulty); // create a sting with difficulty * "0"
        while (!hash.substring(0, difficulty).equals(target)) {
            nonce++;
            hash = calculateHash();
        }
        System.out.println("Block mined! : " + hash);
    }

    //Ad transactions to this block
    public boolean addTransaction(Transaction transaction) {
        //process transaction and check if valid, unless block is genesis block then ignore
        if (transaction == null) return false;
        if ((!Objects.equals(previousHash, "0"))){
            if ((!transaction.processTransaction())){
                System.out.println("Transaction processing failed. Discarded");
                return false;
            }
        }
        transactions.add(transaction);
        System.out.println("Transaction successfully added to block!");
        return true;
    }
}
