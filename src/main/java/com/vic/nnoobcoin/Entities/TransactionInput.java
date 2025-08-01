package com.vic.nnoobcoin.Entities;



import jakarta.persistence.*;

@Entity
@Table(name = "transaction_inputs")
public class TransactionInput {

    @Id
    @GeneratedValue(strategy = GenerationType.IDENTITY)
    private Long id;

    private String referencedOutputId;

    @ManyToOne(fetch = FetchType.LAZY)
    @JoinColumn(name = "transaction_id", referencedColumnName = "transactionId")
    private Transaction transaction;

    private Boolean spent;

    public TransactionInput() {}

    public TransactionInput(String referencedOutputId, Transaction transaction, Boolean spent) {
        this.referencedOutputId = referencedOutputId;
        this.transaction = transaction;
        this.spent = spent;
    }

    public Boolean getSpent() {
        return spent;
    }

    public void setSpent(Boolean spent) {
        this.spent = spent;
    }

    // Constructors, getters, setters

    public Long getId() {
        return id;
    }

    public void setId(Long id) {
        this.id = id;
    }

    public String getReferencedOutputId() {
        return referencedOutputId;
    }

    public void setReferencedOutputId(String referencedOutputId) {
        this.referencedOutputId = referencedOutputId;
    }

    public Transaction getTransaction() {
        return transaction;
    }

    public void setTransaction(Transaction transaction) {
        this.transaction = transaction;
    }
}
