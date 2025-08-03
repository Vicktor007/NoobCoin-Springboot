package com.vic.nnoobcoin.Services;


import com.vic.nnoobcoin.Entities.Block;
import com.vic.nnoobcoin.Entities.Transaction;
import com.vic.nnoobcoin.Repositories.BlockRepository;
import com.vic.nnoobcoin.Repositories.TransactionRepository;
import com.vic.nnoobcoin.utility.StringUtil;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;

import java.security.PrivateKey;
import java.util.ArrayList;
import java.util.Date;


@Service
public class BlockService {

    private final TransactionRepository transactionRepository;
    private final TransactionService transactionService;

    public BlockService(TransactionRepository transactionRepository, TransactionService transactionService) {
        this.transactionRepository = transactionRepository;
        this.transactionService = transactionService;

    }

    public String calculateHash(Block block) {
        return StringUtil.applySha256(
                block.getPreviousHash() +
                        block.getTimestamp() +
                        block.getNonce() +
                        block.getMerkleRoot()
        );
    }

    public String mineBlock(Block block, int difficulty) {
        String target = StringUtil.getDificultyString(difficulty);
        block.setMerkleRoot(StringUtil.getMerkleRoot(new ArrayList<>(block.getTransactions())));
        int nonce = block.getNonce() != null ? block.getNonce() : 0;

        String hash = calculateHash(block);
        while (!hash.substring(0, difficulty).equals(target)) {
            nonce++;
            block.setNonce(nonce);
            hash = calculateHash(block);
        }

        System.out.println("Block mined! : " + hash);

        return hash;


    }

    @Transactional
    public boolean addTransaction(Block block, Transaction transaction, PrivateKey privateKey) throws Exception {
        if (transaction == null) return false;

        if (!"0".equals(block.getPreviousHash())) {
            try {
                Transaction processed = transactionService.processTransaction(transaction, privateKey);
                if (processed == null) {
                    System.out.println("Transaction processing failed. Discarded");
                    return false;
                }
            } catch (Exception e) {
                System.out.println("Transaction processing error: " + e.getMessage());
                return false;
            }
        }

        transaction.setBlock(block);
        block.getTransactions().add(transaction);
        transactionRepository.save(transaction);

        System.out.println("Transaction successfully added to block!");
        return true;
    }

    public Block createNewBlock(String previousHash) {
        Block block = new Block();
        block.setPreviousHash(previousHash);
        block.setTimestamp(new Date().getTime());
        block.setNonce(0);
        block.setTransactions(new ArrayList<>());
        block.setHash(calculateHash(block));
        return block;
    }
}
