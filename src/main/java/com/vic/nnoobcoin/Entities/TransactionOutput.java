package com.vic.nnoobcoin.Entities;



import jakarta.persistence.*;

@Entity
@Table(name = "transaction_outputs")
public class TransactionOutput {

    @Id
    @GeneratedValue(strategy = GenerationType.IDENTITY)
    private Long id;

    @Column(unique = true)
    private String outputId;

    private String recipientAddress;

    private Float value;

    private Boolean isSpent = false;

    @ManyToOne
    @JoinColumn(name = "parent_transaction_id", referencedColumnName = "transactionId")
    private Transaction parentTransaction;

    // Constructors, getters, setters
    public TransactionOutput() {}

    public TransactionOutput(Long id, String outputId, String recipientAddress, Float value, Boolean isSpent, Transaction parentTransaction) {
        this.id = id;
        this.outputId = outputId;
        this.recipientAddress = recipientAddress;
        this.value = value;
        this.isSpent = isSpent;
        this.parentTransaction = parentTransaction;
    }

    public Long getId() {
        return id;
    }

    public void setId(Long id) {
        this.id = id;
    }

    public String getOutputId() {
        return outputId;
    }

    public void setOutputId(String outputId) {
        this.outputId = outputId;
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

    public Boolean getSpent() {
        return isSpent;
    }

    public void setSpent(Boolean spent) {
        isSpent = spent;
    }

    public Transaction getParentTransaction() {
        return parentTransaction;
    }

    public void setParentTransaction(Transaction parentTransaction) {
        this.parentTransaction = parentTransaction;
    }
}
