package com.vic.nnoobcoin.Repositories;

import com.vic.nnoobcoin.Entities.Wallet;
import org.springframework.data.jpa.repository.JpaRepository;

public interface WalletRepository extends JpaRepository<Wallet, Long> {
    Wallet findByPublicKey(String publicKey);
}

