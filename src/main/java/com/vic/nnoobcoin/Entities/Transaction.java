package com.vic.nnoobcoin.Entities;



import com.fasterxml.jackson.annotation.JsonBackReference;
import com.fasterxml.jackson.annotation.JsonIgnore;
import com.vic.nnoobcoin.utility.StringUtil;
import jakarta.persistence.*;

import java.security.PrivateKey;
import java.util.Base64;
import java.util.List;

@Entity
@Table(name = "transactions")
public class Transaction {

    @Id
    @GeneratedValue(strategy = GenerationType.IDENTITY)
    private Long id;

    @Column(unique = true)
    public String transactionID;

    private String senderAddress;

    private String recipientAddress;

    private Float value;

    @Column(length = 2048)
    private String signature;

    @ManyToOne
    @JoinColumn(name = "block_hash", referencedColumnName = "hash")
    @JsonBackReference
    private Block block;

    @OneToMany(mappedBy = "transaction", cascade = CascadeType.ALL, orphanRemoval = true)
    private List<TransactionInput> inputs;

    @OneToMany(mappedBy = "parentTransaction", cascade = CascadeType.ALL)
    private List<TransactionOutput> outputs;

    // Constructors, getters, setters
    public Transaction() {}

    public Transaction(Long id, String transactionId, String senderAddress, String recipientAddress, Float value, String signature, Block block, List<TransactionInput> inputs, List<TransactionOutput> outputs) {
        this.id = id;
        this.transactionID = transactionId;
        this.senderAddress = senderAddress;
        this.recipientAddress = recipientAddress;
        this.value = value;
        this.signature = signature;
        this.block = block;
        this.inputs = inputs;
        this.outputs = outputs;
    }

    public Long getId() {
        return id;
    }

    public void setId(Long id) {
        this.id = id;
    }

    public String getTransactionId() {
        return transactionID;
    }

    public void setTransactionId(String transactionId) {
        this.transactionID = transactionId;
    }

    public String getSenderAddress() {
        return senderAddress;
    }

    public void setSenderAddress(String senderAddress) {
        this.senderAddress = senderAddress;
    }

    public String getRecipientAddress() {
        return recipientAddress;
    }

    public void setRecipientAddress(String recipientAddress) {
        this.recipientAddress = recipientAddress;
    }

    public Float getValue() {
        return value;
    }

    public void setValue(Float value) {
        this.value = value;
    }

    public String getSignature() {
        return signature;
    }

    public void setSignature(String signature) {
        this.signature = signature;
    }

    public Block getBlock() {
        return block;
    }

    public void setBlock(Block block) {
        this.block = block;
    }

    public List<TransactionInput> getInputs() {
        return inputs;
    }

    public void setInputs(List<TransactionInput> inputs) {
        this.inputs = inputs;
    }

    public List<TransactionOutput> getOutputs() {
        return outputs;
    }

    public void setOutputs(List<TransactionOutput> outputs) {
        this.outputs = outputs;
    }

    // signs all the data we dont wish to be tampered with
    public void generateSignature(PrivateKey privateKey) {
        String data = senderAddress + recipientAddress + value;
        byte[] signatureBytes = StringUtil.applyECDSASig(privateKey, data);
        this.signature = Base64.getEncoder().encodeToString(signatureBytes); // returns Base64 string
    }

    // Generate the transactionId using a SHA256 hash and a timestamp
    public void generateTransactionId() {
        long timestamp = System.currentTimeMillis();
        String data = senderAddress + recipientAddress + value + timestamp;
        this.transactionID = StringUtil.applySha256(data);
    }

}
