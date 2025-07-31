package com.vic.nnoobcoin.Repositories;



import com.vic.nnoobcoin.Entities.Block;
import org.springframework.data.jpa.repository.JpaRepository;

public interface BlockRepository extends JpaRepository<Block, Long> {
    Block findByHash(String hash);
}

