package com.vic.nnoobcoin.Services;


import com.vic.nnoobcoin.Entities.Transaction;
import com.vic.nnoobcoin.Entities.TransactionInput;
import com.vic.nnoobcoin.Entities.TransactionOutput;
import com.vic.nnoobcoin.Entities.Wallet;
import com.vic.nnoobcoin.Repositories.TransactionOutputRepository;
import com.vic.nnoobcoin.Repositories.TransactionRepository;
import com.vic.nnoobcoin.Repositories.WalletRepository;
import com.vic.nnoobcoin.utility.CryptoUtil;
import com.vic.nnoobcoin.utility.StringUtil;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.stereotype.Service;

import java.security.*;
import java.security.spec.ECGenParameterSpec;
import java.util.ArrayList;
import java.util.List;

@Service
public class WalletService {

    private PrivateKey privateKey;
    private PublicKey publicKey;

    @Autowired
    private TransactionOutputRepository transactionOutputRepository;

    @Autowired
    private TransactionRepository transactionRepository;

    @Autowired
    private WalletRepository walletRepository;

    public Wallet createWallet(String passphrase) throws Exception {
        generateKeyPair();
        String pubKeyStr = StringUtil.getStringFromKey(publicKey);
        String privKeyStr = StringUtil.getStringFromKey(privateKey);

        String encryptedPrivKey = CryptoUtil.encrypt(privKeyStr, passphrase);

        Wallet wallet = new Wallet();
        wallet.setPublicKey(pubKeyStr);
        wallet.setEncryptedPrivateKey(encryptedPrivKey);
        walletRepository.save(wallet);

        return wallet;
    }

    private void generateKeyPair() {
        try {
            KeyPairGenerator keyGen = KeyPairGenerator.getInstance("ECDSA", "BC");
            SecureRandom random = SecureRandom.getInstance("SHA1PRNG");
            ECGenParameterSpec ecSpec = new ECGenParameterSpec("prime192v1");

            keyGen.initialize(ecSpec, random);
            KeyPair keyPair = keyGen.generateKeyPair();
            privateKey = keyPair.getPrivate();
            publicKey = keyPair.getPublic();
        } catch (Exception e) {
            throw new RuntimeException(e);
        }
    }

    public float getBalance() {
        String pubKeyStr = StringUtil.getStringFromKey(publicKey);
        List<TransactionOutput> utxos = transactionOutputRepository.findByRecipientAddressAndIsSpentFalse(pubKeyStr);
        float total = 0;
        for (TransactionOutput utxo : utxos) {
            total += utxo.getValue();
        }
        return total;
    }


    public Transaction sendFunds(PublicKey recipient, float value) {
        float balance = getBalance();
        if (balance < value) {
            System.out.println("Not enough funds to send this transaction.");
            return null;
        }

        // Get UTXOs for this wallet
        List<TransactionOutput> utxos = transactionOutputRepository
                .findByRecipientAddressAndIsSpentFalse(StringUtil.getStringFromKey(publicKey));

        List<TransactionInput> inputs = new ArrayList<>();
        float total = 0;

        // Create the transaction object first
        Transaction transaction = new Transaction();
        transaction.setSenderAddress(StringUtil.getStringFromKey(publicKey));
        transaction.setRecipientAddress(StringUtil.getStringFromKey(recipient));
        transaction.setValue(value);

        for (TransactionOutput utxo : utxos) {
            total += utxo.getValue();

            TransactionInput input = new TransactionInput();
            input.setReferencedOutputId(utxo.getOutputId());
            input.setTransaction(transaction); // <---- KEY: Set the back-reference

            inputs.add(input);

            utxo.setSpent(true);
            transactionOutputRepository.save(utxo);

            if (total >= value) break;
        }

        transaction.setInputs(inputs);
        transaction.generateSignature(privateKey);

        transactionRepository.save(transaction); // thanks to CascadeType.ALL, inputs will be saved too
        return transaction;
    }

    public PrivateKey loadPrivateKey(Wallet wallet, String passphrase) {
        try {
            String decryptedKey = CryptoUtil.decrypt(wallet.getEncryptedPrivateKey(), passphrase);
            return StringUtil.getPrivateKeyFromString(decryptedKey);
        } catch (Exception e) {
            throw new RuntimeException("Failed to decrypt private key");
        }
    }



//    public Transaction sendFunds(PublicKey recipient, float value) {
//        float balance = getBalance();
//        if (balance < value) {
//            System.out.println("Not enough funds to send this transaction.");
//            return null;
//        }
//
//        List<TransactionOutput> utxos = transactionOutputRepository.findByRecipientAddressAndIsSpentFalse(StringUtil.getStringFromKey(publicKey));
//        List<TransactionInput> inputs = new ArrayList<>();
//        float total = 0;
//
//        for (TransactionOutput utxo : utxos) {
//            total += utxo.getValue();
//            TransactionInput input = new TransactionInput();
//            input.setReferencedOutputId(utxo.getOutputId());
//            inputs.add(input);
//            utxo.setSpent(true);
//            transactionOutputRepository.save(utxo);
//            if (total >= value) break;
//        }
//
//        Transaction transaction = new Transaction();
//        transaction.setSenderAddress(StringUtil.getStringFromKey(publicKey));
//        transaction.setRecipientAddress(StringUtil.getStringFromKey(recipient));
//        transaction.setValue(value);
//        transaction.setInputs(inputs);
//        transaction.generateSignature(privateKey); // implement in your Transaction class
//
//        transactionRepository.save(transaction);
//        return transaction;
//    }
}
