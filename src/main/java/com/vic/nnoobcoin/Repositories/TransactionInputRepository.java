package com.vic.nnoobcoin.Repositories;

import com.vic.nnoobcoin.Entities.TransactionInput;
import org.springframework.data.jpa.repository.JpaRepository;

import java.util.List;

public interface TransactionInputRepository extends JpaRepository<TransactionInput, Long> {
    List<TransactionInput> findByTransaction_TransactionID(String transactionId);
}

