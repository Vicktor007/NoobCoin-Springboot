package com.vic.nnoobcoin.Repositories;



import com.vic.nnoobcoin.Entities.Block;
import org.springframework.data.jpa.repository.JpaRepository;

import java.util.List;

public interface BlockRepository extends JpaRepository<Block, Long> {
    List<Block> findAllByOrderByIdAsc();

    Block findByHash(String hash);
}

