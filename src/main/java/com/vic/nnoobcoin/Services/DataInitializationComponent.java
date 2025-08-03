package com.vic.nnoobcoin.Services;

import com.google.gson.GsonBuilder;
import com.vic.nnoobcoin.Entities.*;
import com.vic.nnoobcoin.Repositories.*;
import com.vic.nnoobcoin.utility.StringUtil;
import org.bouncycastle.jce.provider.BouncyCastleProvider;
import org.springframework.boot.CommandLineRunner;
import org.springframework.stereotype.Component;
import org.springframework.transaction.annotation.Transactional;

import java.security.Security;
import java.util.ArrayList;
import java.util.HashMap;
import java.util.List;

/**
 * Initializes blockchain data on application startup using JPA repositories.
 */
@Component
public class DataInitializationComponent implements CommandLineRunner {

    private final BlockRepository blockRepository;
    private final TransactionRepository transactionRepository;
    private final TransactionOutputRepository transactionOutputRepository;
    private final WalletService walletService;

    private static final int DIFFICULTY = 5;
    private static final float MINIMUM_TRANSACTION = 0.1f;

    // In-memory UTXO map for validation
    private final HashMap<String, TransactionOutput> UTXOs = new HashMap<>();
    private final TransactionService transactionService;
    private final BlockService blockService;

    public DataInitializationComponent(
            BlockRepository blockRepository,
            TransactionRepository transactionRepository,
            TransactionOutputRepository transactionOutputRepository, WalletService walletService, TransactionService transactionService, BlockService blockService) {
        this.blockRepository = blockRepository;
        this.transactionRepository = transactionRepository;
        this.transactionOutputRepository = transactionOutputRepository;
        this.walletService = walletService;
        this.transactionService = transactionService;
        this.blockService = blockService;
    }

    @Override
    @Transactional
    public void run(String... args) throws Exception {
        initialize();
        validateChain();
        printChain();
    }

    private void initialize() throws Exception {
        Security.addProvider(new BouncyCastleProvider());

        Wallet walletA = walletService.createWallet("jonas");
        Wallet walletB = walletService.createWallet("elias");
        Wallet coinbase = walletService.createWallet("coinbase");

        System.out.println("Public key: " + walletA.getPublicKey());
        System.out.println("Private key: " + walletService.loadPrivateKey(walletA, "jonas"));

        // Create genesis transaction
        Transaction genesisTx = new Transaction();
              genesisTx.setSenderAddress(coinbase.getPublicKey());
                genesisTx.setRecipientAddress(walletA.getPublicKey());
                genesisTx.setValue(100f);



        genesisTx.generateSignature(walletService.loadPrivateKey(coinbase, "coinbase"));
        genesisTx.generateTransactionId();

        TransactionOutput genesisOutput = new TransactionOutput();
        genesisOutput.setOutputId(StringUtil.applySha256(genesisTx.getRecipientAddress() + genesisTx.transactionID));
        genesisOutput.setRecipientAddress(genesisTx.getRecipientAddress());
        genesisOutput.setValue(genesisTx.getValue());
        genesisOutput.setSpent(false);
        genesisOutput.setParentTransaction(genesisTx);

        genesisTx.setOutputs(new ArrayList<>());
        genesisTx.getOutputs().add(genesisOutput);

        UTXOs.put(String.valueOf(genesisOutput.getId()), genesisOutput);

        // Persist genesis entities
        transactionRepository.save(genesisTx);
        transactionOutputRepository.save(genesisOutput);
        Block genesisBlock = new Block();
        genesisBlock.setId(Long.valueOf("0"));
        genesisBlock.setTransactions(List.of(genesisTx));
        blockService.mineBlock(genesisBlock, DIFFICULTY);
        blockRepository.save(genesisBlock);

        // Continue with block1
        String pass ;
        Block block1 = createAndSaveBlock(genesisBlock, walletA, walletB, 40f, "jonas");
        Block block2 = createAndSaveBlock(block1, walletA, walletB, 1000f, "jonas");
        Block block3 = createAndSaveBlock(block2, walletB, walletA, 20f, "elias");
    }

    private Block createAndSaveBlock(Block previousBlock,
                                     Wallet fromWallet,
                                     Wallet toWallet,
                                     float amount, String walletPassphrase) throws Exception {

        Block newBlock =
        blockService.createNewBlock(previousBlock.getHash());
        blockService.addTransaction(newBlock,
                walletService.sendFunds(StringUtil.getPublicKeyFromString(toWallet.getPublicKey()), amount), walletService.loadPrivateKey(fromWallet, walletPassphrase)
        );
        blockService.mineBlock(newBlock, DIFFICULTY);
        blockRepository.save(newBlock);
        // Save any new transactions/outputs
        for (Transaction tx : newBlock.getTransactions()) {
            transactionRepository.save(tx);
            for (TransactionOutput out : tx.getOutputs()) {
                transactionOutputRepository.save(out);
                UTXOs.put(String.valueOf(out.getId()), out);
            }
        }
        return newBlock;
    }

    private void validateChain() {
        List<Block> chain = blockRepository.findAll();
        // ... (add validation logic similar to isChainValid, using UTXOs map) ...
        System.out.println("Blockchain persisted and validated.");
    }

    private void printChain() {
        List<Block> chain = blockRepository.findAll();
        String json = new GsonBuilder().setPrettyPrinting().create().toJson(chain);
        System.out.println("\nThe blockchain is: \n" + json);
    }
}
