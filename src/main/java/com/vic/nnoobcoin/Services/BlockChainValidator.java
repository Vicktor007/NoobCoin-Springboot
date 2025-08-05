package com.vic.nnoobcoin.Services;

import com.vic.nnoobcoin.Entities.*;
import com.vic.nnoobcoin.Repositories.BlockRepository;
import com.vic.nnoobcoin.utility.StringUtil;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;

import java.util.HashMap;
import java.util.List;

@Service
public class BlockChainValidator {

    private final BlockRepository blockRepository;
    private final BlockService blockService;
    private static final int DIFFICULTY = 5;

    public BlockChainValidator(BlockRepository blockRepository, BlockService blockService) {
        this.blockRepository = blockRepository;
        this.blockService = blockService;
    }



        public boolean validateChain(List<Block> chain, int difficulty) {
            if (chain.isEmpty()) {
                System.out.println("Blockchain is empty.");
                return false;
            }

            // ✅ Ensure only one genesis block
            long genesisCount = chain.stream()
                    .filter(b -> b.getPreviousHash() == null)
                    .count();
            if (genesisCount > 1) {
                System.out.println("❌ More than one genesis block found.");
                return false;
            }

            String hashTarget = new String(new char[difficulty]).replace('\0', '0');
            HashMap<String, TransactionOutput> tempUTXOs = new HashMap<>();

            for (int i = 0; i < chain.size(); i++) {
                Block currentBlock = chain.get(i);

                String calculatedHash = blockService.calculateHash(currentBlock);
                if (!currentBlock.getHash().equals(calculatedHash)) {
                    System.out.println("❌ Invalid hash at block " + currentBlock.getId());
                    return false;
                }

                if (i > 0) {
                    Block previousBlock = chain.get(i - 1);
                    // ✅ Fix NPE by reversing equals check
                    if (!previousBlock.getHash().equals(currentBlock.getPreviousHash())) {
                        System.out.println("❌ Previous hash mismatch at block " + currentBlock.getId());
                        return false;
                    }
                } else if (currentBlock.getPreviousHash() != null) {
                    System.out.println("❌ Genesis block has non-null previous hash.");
                    return false;
                }

                // Proof of Work check
                if (!currentBlock.getHash().substring(0, difficulty).equals(hashTarget)) {
                    System.out.println("❌ Block " + currentBlock.getId() + " not properly mined.");
                    return false;
                }

                // Transaction validations
                for (Transaction tx : currentBlock.getTransactions()) {
                    if (!verifySignature(tx)) {
                        System.out.println("❌ Invalid signature in transaction " + tx.getId());
                        return false;
                    }

                    if (getInputsValue(tx, tempUTXOs) != getOutputsValue(tx)) {
                        System.out.println("❌ Inputs ≠ Outputs in transaction " + tx.getId());
                        return false;
                    }

                    if (tx.getInputs() != null) {
                        for (TransactionInput input : tx.getInputs()) {
                            TransactionOutput referenced = tempUTXOs.get(input.getReferencedOutputId());
                            if (referenced == null) {
                                System.out.println("❌ Missing input in transaction " + tx.getId());
                                return false;
                            }
                            tempUTXOs.remove(input.getReferencedOutputId());
                        }
                    }

                    for (TransactionOutput output : tx.getOutputs()) {
                        tempUTXOs.put(output.getOutputId(), output);
                    }

                    if (!tx.getOutputs().isEmpty()) {
                        if (!tx.getOutputs().get(0).getRecipientAddress().equals(tx.getRecipientAddress())) {
                            System.out.println("❌ Wrong recipient in transaction " + tx.getId());
                            return false;
                        }

                        if (tx.getOutputs().size() > 1 &&
                                !tx.getOutputs().get(1).getRecipientAddress().equals(tx.getSenderAddress())) {
                            System.out.println("❌ Change output not to sender in transaction " + tx.getId());
                            return false;
                        }
                    }
                }
            }

            System.out.println("✅ Blockchain is valid.");
            return true;
        }




    public float getInputsValue(Transaction tx, HashMap<String, TransactionOutput> utxos) {
        if (tx.getInputs() == null) return 0f;
        float total = 0f;
        for (TransactionInput input : tx.getInputs()) {
            TransactionOutput utxo = utxos.get(input.getReferencedOutputId());
            if (utxo != null) {
                total += utxo.getValue();
            }
        }
        return total;
    }

    public float getOutputsValue(Transaction tx) {
        if (tx.getOutputs() == null) return 0f;
        float total = 0f;
        for (TransactionOutput output : tx.getOutputs()) {
            total += output.getValue();
        }
        return total;
    }


    // verifies the data we signed hasnt been tampered with
    public boolean verifySignature(Transaction tx) {
        String data =tx.getSenderAddress() +tx.getRecipientAddress() + tx.getValue();
        return StringUtil.verifyECDSASig(StringUtil.getPublicKeyFromString(tx.getSenderAddress()), data, StringUtil.getBytesFromSignatureString(tx.getSignature()));
    }
}
