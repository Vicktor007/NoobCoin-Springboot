package com.vic.nnoobcoin.Repositories;

import com.vic.nnoobcoin.Entities.TransactionOutput;
import org.springframework.data.jpa.repository.JpaRepository;

import java.util.List;

public interface TransactionOutputRepository extends JpaRepository<TransactionOutput, Long> {
    TransactionOutput findByOutputId(String outputId);
    List<TransactionOutput> findByRecipientAddressAndIsSpentFalse(String recipientAddress);
    List<TransactionOutput> findByIsSpentFalse();
}

