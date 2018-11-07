package com.aethercoder.dao;

import com.aethercoder.entity.AddressInfo;
import com.aethercoder.entity.BlockInfo;
import org.springframework.data.jpa.repository.JpaRepository;
import org.springframework.data.jpa.repository.Query;
import org.springframework.stereotype.Repository;

import java.math.BigInteger;
import java.util.List;

/**
 * Created by king on 11/09/2018.
 */
@Repository
public interface AddressInfoDao extends JpaRepository<AddressInfo, Long>{

    @Query(value = "select max(block_height) from t_address_info", nativeQuery = true)
    Long findMaxBlockHeight();

    @Query(value = "select DISTINCT(block_height) from t_address_info ORDER BY block_height asc", nativeQuery = true)
    List<BigInteger> getAllBlockHeightFromDB();
}
