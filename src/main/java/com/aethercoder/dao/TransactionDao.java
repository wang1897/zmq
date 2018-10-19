package com.aethercoder.dao;

import com.aethercoder.entity.Transaction;
import org.springframework.data.jpa.repository.JpaRepository;
import org.springframework.data.jpa.repository.Query;
import org.springframework.stereotype.Repository;

import java.math.BigInteger;
import java.util.List;

/**
 * Created by hepengfei on 23/01/2018.
 */
@Repository
public interface TransactionDao extends JpaRepository<Transaction, Long> {
    List<Transaction> findByTimeIsNullOrBlockHeightAfter(Long blockHeight);
    List<Transaction> findByBlockHeight(Long blockHeight);

    @Query(value = "select max(block_height) from transaction", nativeQuery = true)
    Long findMaxBlockHeight();

    @Query(value = "select DISTINCT(block_height) from transaction ORDER BY block_height asc", nativeQuery = true)
    List<BigInteger> getAllBlockHeightFromDB();
}