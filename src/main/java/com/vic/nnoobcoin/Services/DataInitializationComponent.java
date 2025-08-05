package com.vic.nnoobcoin.Services;

import com.fasterxml.jackson.core.JsonProcessingException;
import com.fasterxml.jackson.databind.ObjectMapper;
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


    private final ObjectMapper objectMapper;
    private final BlockRepository blockRepository;
    private final TransactionRepository transactionRepository;
    private final TransactionOutputRepository transactionOutputRepository;
    private final WalletService walletService;
    private final BlockChainValidator blockChainValidator;

    private static final int DIFFICULTY = 5;
    private static final float MINIMUM_TRANSACTION = 0.1f;

    // In-memory UTXO map for validation
    private final HashMap<String, TransactionOutput> UTXOs = new HashMap<>();
    private final BlockService blockService;

    public DataInitializationComponent(
            ObjectMapper objectMapper, BlockRepository blockRepository,
            TransactionRepository transactionRepository,
            TransactionOutputRepository transactionOutputRepository, WalletService walletService, BlockChainValidator blockChainValidator, BlockService blockService) {
        this.objectMapper = objectMapper;
        this.blockRepository = blockRepository;
        this.transactionRepository = transactionRepository;
        this.transactionOutputRepository = transactionOutputRepository;
        this.walletService = walletService;
        this.blockChainValidator = blockChainValidator;
        this.blockService = blockService;
    }

    @Override
    @Transactional
    public void run(String... args) throws Exception {
        initialize();
//        blockChainValidator.validateChain();
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

        // Check if genesis block already exists
        if (blockRepository.count() == 0) {
            // Persist genesis entities
            transactionRepository.save(genesisTx);
            transactionOutputRepository.save(genesisOutput);

            Block genesisBlock = blockService.createGenesisBlock(genesisTx);

            // Continue with block1
            Block block1 = createAndSaveBlock(genesisBlock, walletA, walletB, 40f, "jonas");
            Block block2 = createAndSaveBlock(block1, walletA, walletB, 1000f, "jonas");
            Block block3 = createAndSaveBlock(block2, walletB, walletA, 20f, "elias");

            System.out.println("✅ Blockchain initialized from scratch.");
        } else {
            System.out.println("✅ Blockchain already exists. Skipping genesis block creation.");
        }
    }

        @Transactional
    public Block createAndSaveBlock(Block previousBlock,
                                     Wallet fromWallet,
                                     Wallet toWallet,
                                     float amount, String walletPassphrase) throws Exception {

        Block newBlock =
        blockService.createNewBlock(previousBlock.getHash());
        blockService.addTransaction(newBlock,
                walletService.sendFunds(StringUtil.getPublicKeyFromString(toWallet.getPublicKey()), amount), walletService.loadPrivateKey(fromWallet, walletPassphrase)
        );
           String hash = blockService.mineBlock(newBlock, DIFFICULTY);
           newBlock.setHash(hash);
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


    @Transactional(readOnly = true)
    public void validateChain() {
        List<Block> chain = blockRepository.findAllByOrderByIdAsc(); // use ordered chain
        blockChainValidator.validateChain(chain, DIFFICULTY);
    }




    private void printChain() throws JsonProcessingException {
        List<Block> chain = blockRepository.findAll();
        String json = objectMapper.writerWithDefaultPrettyPrinter().writeValueAsString(chain);
        System.out.println(json);
    }

}
