package com.vic.nnoobcoin.Entities;



import jakarta.persistence.*;

@Entity
@Table(name = "transaction_inputs")
public class TransactionInput {

    @Id
    @GeneratedValue(strategy = GenerationType.IDENTITY)
    private Long id;

    private String referencedOutputId;

    @ManyToOne
    @JoinColumn(name = "transaction_id", referencedColumnName = "transactionId")
    private Transaction transaction;

    // Constructors, getters, setters
    public TransactionInput() {}

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
