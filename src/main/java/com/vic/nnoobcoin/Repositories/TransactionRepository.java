package com.vic.nnoobcoin.Repositories;

import com.vic.nnoobcoin.Transaction;
import org.springframework.data.jpa.repository.JpaRepository;

import java.security.PublicKey;
import java.util.List;

public interface TransactionRepository extends JpaRepository<Transaction, Long> {
    Transaction findByTransactionID(String transactionId);
    List<Transaction> findBySenderAddress(PublicKey senderAddress);
    List<Transaction> findByRecipientAddress(PublicKey recipientAddress);
}

