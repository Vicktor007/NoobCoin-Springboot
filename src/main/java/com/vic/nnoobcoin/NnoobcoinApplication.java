package com.vic.nnoobcoin;

import com.google.gson.GsonBuilder;
import com.vic.nnoobcoin.Repositories.BlockRepository;
import com.vic.nnoobcoin.Repositories.TransactionOutputRepository;
import com.vic.nnoobcoin.Repositories.TransactionRepository;
import org.bouncycastle.jce.provider.BouncyCastleProvider;
import org.springframework.boot.SpringApplication;
import org.springframework.boot.autoconfigure.SpringBootApplication;

import java.security.Security;
import java.util.ArrayList;
import java.util.HashMap;

@SpringBootApplication
public class NnoobcoinApplication {

    private BlockRepository  blockRepository;

    private TransactionRepository transactionRepository;

    private TransactionOutputRepository transactionOutputRepository;

    public NnoobcoinApplication(TransactionRepository transactionRepository, BlockRepository blockRepository, TransactionOutputRepository transactionOutputRepository) {
        this.blockRepository = blockRepository;
        this.transactionRepository = transactionRepository;
        this.transactionOutputRepository = transactionOutputRepository;
    }
    public static ArrayList<Block> blockChain = new ArrayList<Block>();
    public static HashMap<String,TransactionOutput> UTXOS = new HashMap<String, TransactionOutput>();

    public static int difficulty = 5;
    public static float minimumTransaction = 0.1f;
    public static Wallet walletA;
    public static Wallet walletB;
    public static Transaction genesisTransaction;

    public static void main(String[] args) {
        SpringApplication.run(NnoobcoinApplication.class, args);

        // Setup bouncey castle as a security provider
        Security.addProvider(new BouncyCastleProvider());

        // create the new wallets
        walletA = new Wallet();
        walletB = new Wallet();
        Wallet coinbase = new Wallet();

        //Test public and private keys
        System.out.println("Public key: " + walletA.publicKey);
        System.out.println("Private key: " + walletA.privateKey);

        //create genesis transaction, which sends 100 NoobCoins to walletA:
        genesisTransaction = new Transaction(coinbase.publicKey, walletA.publicKey, 100f, null);
        genesisTransaction.generateSignature(coinbase.privateKey); //manually signs the genesis transaction
        genesisTransaction.transactionID = "0"; //manually set the transaction id
        //manually add the transaction output
        genesisTransaction.outputs.add(new TransactionOutput(genesisTransaction.recipientAddress,genesisTransaction.value, genesisTransaction.transactionID));
        UTXOS.put(genesisTransaction.outputs.getFirst().id, genesisTransaction.outputs.getFirst());


        System.out.println("Creating and mining genesis block...");
        Block genesis = new Block("0");
        genesis.addTransaction(genesisTransaction);
        blockChain.add(genesis);

        Block block1 = new Block(genesis.hash);
        System.out.println("\nWalletA's balance is : " +  walletA.getBalance());
        System.out.println("\nWalletA is attempting to send funds (40) to walletB...");
        block1.addTransaction(walletA.sendFunds(walletB.publicKey,40f));
        addBlock(block1);
        System.out.println("\nWalletA's balance is: " + walletA.getBalance());
        System.out.println("WalletB's balance is: " + walletB.getBalance());

        Block block2 = new Block(block1.hash);
        System.out.println("\nWalletA Attempting to send more funds (1000) than it has...");
        block2.addTransaction(walletA.sendFunds(walletB.publicKey, 1000f));
        addBlock(block2);
        System.out.println("\nWalletA's balance is: " + walletA.getBalance());
        System.out.println("WalletB's balance is: " + walletB.getBalance());

        Block block3 = new Block(block2.hash);
        System.out.println("\nWalletB is Attempting to send funds (20) to WalletA...");
        block3.addTransaction(walletB.sendFunds( walletA.publicKey, 20));
        System.out.println("\nWalletA's balance is: " + walletA.getBalance());
        System.out.println("WalletB's balance is: " + walletB.getBalance());

        isChainValid();

        String blockChainJson = new GsonBuilder().setPrettyPrinting().create().toJson(blockChain);
        System.out.println("\nThe blockchain is: ");
        System.out.println(blockChainJson);



    }
    public static void isChainValid() {
        Block currentBlock;
        Block previousBlock;
        String hashTarget = new String(new char[difficulty]).replace('\0', '0');
        HashMap<String,TransactionOutput> tempUTXOs = new HashMap<String,TransactionOutput>(); //a temporary working list of unspent transactions at a given block state.
        tempUTXOs.put(genesisTransaction.outputs.getFirst().id, genesisTransaction.outputs.getFirst());

        //loop through blockchain to check hashes:
        for(int i=1; i < blockChain.size(); i++) {

            currentBlock = blockChain.get(i);
            previousBlock = blockChain.get(i-1);
            //compare registered hash and calculated hash:
            if(!currentBlock.hash.equals(currentBlock.calculateHash()) ){
                System.out.println("#Current Hashes not equal");
                return;
            }
            //compare previous hash and registered previous hash
            if(!previousBlock.hash.equals(currentBlock.previousHash) ) {
                System.out.println("#Previous Hashes not equal");
                return;
            }
            //check if hash is solved
            if(!currentBlock.hash.substring( 0, difficulty).equals(hashTarget)) {
                System.out.println("#This block hasn't been mined");
                return;
            }

            //loop through blockchains transactions:
            TransactionOutput tempOutput;
            for(int t=0; t <currentBlock.transactions.size(); t++) {
                Transaction currentTransaction = currentBlock.transactions.get(t);

                if(!currentTransaction.isSignatureValid()) {
                    System.out.println("#Signature on Transaction(" + t + ") is Invalid");
                    return;
                }
                if(currentTransaction.getInputsValue() != currentTransaction.getOutputsValue()) {
                    System.out.println("#Inputs are not equal to outputs on Transaction(" + t + ")");
                    return;
                }

                for(TransactionInput input: currentTransaction.inputs) {
                    tempOutput = tempUTXOs.get(input.transactionOutputID);

                    if(tempOutput == null) {
                        System.out.println("#Referenced input on Transaction(" + t + ") is Missing");
                        return;
                    }

                    if(input.UTXO.value != tempOutput.value) {
                        System.out.println("#Referenced input Transaction(" + t + ") value is Invalid");
                        return;
                    }

                    tempUTXOs.remove(input.transactionOutputID);
                }

                for(TransactionOutput output: currentTransaction.outputs) {
                    tempUTXOs.put(output.id, output);
                }

                if( currentTransaction.outputs.get(0).recipient != currentTransaction.recipientAddress) {
                    System.out.println("#Transaction(" + t + ") output reciepient is not who it should be");
                    return;
                }
                if( currentTransaction.outputs.get(1).recipient != currentTransaction.senderAddress) {
                    System.out.println("#Transaction(" + t + ") output 'change' is not sender.");
                    return;
                }

            }

        }
        System.out.println("Blockchain is valid");
    }

    public static void addBlock(Block newBlock) {
        newBlock.mineBlock(difficulty);
        blockChain.add(newBlock);
    }
    }


