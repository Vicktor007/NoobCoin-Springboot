package com.vic.nnoobcoin.Services;

import com.vic.nnoobcoin.Entities.Transaction;
import com.vic.nnoobcoin.Entities.TransactionInput;
import com.vic.nnoobcoin.Entities.TransactionOutput;
import com.vic.nnoobcoin.Repositories.TransactionOutputRepository;
import com.vic.nnoobcoin.Repositories.TransactionRepository;
import com.vic.nnoobcoin.utility.StringUtil;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;

import java.security.PrivateKey;
import java.util.ArrayList;
import java.util.Base64;
import java.util.List;

@Service
public class TransactionService {

    public static final float MINIMUM_TRANSACTION = 0.1f;


    private final TransactionRepository transactionRepository;


    private final TransactionOutputRepository transactionOutputRepository;

    public TransactionService(TransactionRepository transactionRepository, TransactionOutputRepository transactionOutputRepository) {
        this.transactionRepository = transactionRepository;
        this.transactionOutputRepository = transactionOutputRepository;
    }


    @Transactional
    public Transaction processTransaction(Transaction transaction, PrivateKey privateKey) throws Exception {
        // Validate signature
        boolean isValid = StringUtil.verifyECDSASig(
                StringUtil.getPublicKeyFromString(transaction.getSenderAddress()),
                transaction.getSenderAddress() + transaction.getRecipientAddress() + transaction.getValue(),
                Base64.getDecoder().decode(transaction.getSignature())
        );

        if (!isValid) {
            System.out.println("Invalid transaction signature");
            return null;
        }

        // Gather unspent outputs referenced by inputs
        float totalInputValue = 0f;
        List<TransactionOutput> inputUTXOs = new ArrayList<>();
        for (TransactionInput input : transaction.getInputs()) {
            TransactionOutput utxo = transactionOutputRepository.findByOutputId(input.getReferencedOutputId());
            if (utxo == null || utxo.getSpent()) continue;

            totalInputValue += utxo.getValue();
            inputUTXOs.add(utxo);
        }

        if (totalInputValue < MINIMUM_TRANSACTION || totalInputValue < transaction.getValue()) {
            System.out.println("Transaction input value too low");
            return null;
        }

        // Generate transaction ID
        transaction.generateTransactionId();

        System.out.println("transaction id :" + transaction.getTransactionId());
        // Create outputs
        float leftover = totalInputValue - transaction.getValue();

        TransactionOutput recipientOutput = new TransactionOutput();
        recipientOutput.setOutputId(StringUtil.applySha256(transaction.getRecipientAddress() + transaction.getTransactionId()));
        recipientOutput.setRecipientAddress(transaction.getRecipientAddress());
        recipientOutput.setValue(transaction.getValue());
        recipientOutput.setSpent(false);
        recipientOutput.setParentTransaction(transaction);

        TransactionOutput changeOutput = new TransactionOutput();
        changeOutput.setOutputId(StringUtil.applySha256(transaction.getSenderAddress() + transaction.getTransactionId()));
        changeOutput.setRecipientAddress(transaction.getSenderAddress());
        changeOutput.setValue(leftover);
        changeOutput.setSpent(false);
        changeOutput.setParentTransaction(transaction);

        transaction.setOutputs(List.of(recipientOutput, changeOutput));

        // Mark input UTXOs as spent
        for (TransactionOutput utxo : inputUTXOs) {
            utxo.setSpent(true);
            transactionOutputRepository.save(utxo);
        }

        // Save transaction (cascades inputs & outputs)

        return transactionRepository.save(transaction);
    }
}
