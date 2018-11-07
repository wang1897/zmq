package com.aethercoder.dao;

import com.aethercoder.entity.TxInfo;
import org.springframework.data.jpa.repository.JpaRepository;
import org.springframework.data.jpa.repository.Query;
import org.springframework.data.repository.query.Param;
import org.springframework.stereotype.Repository;

import java.util.List;

/**
 * Created by king on 11/09/2018.
 */
@Repository
public interface TxInfoDao extends JpaRepository<TxInfo, Long>{
    @Query(value = "select max(block_height) from t_tx_info", nativeQuery = true)
    Long findMaxBlockHeight();

    List<TxInfo> getByTxId(String txHash);

    @Query(value = "select * from t_tx_info order by time desc LIMIT :limit, :offset",nativeQuery = true)
    List<TxInfo> getByPage(@Param("limit") Integer limit, @Param("offset") Integer offset);

    @Query(value = "SELECT a.* from t_tx_info a,t_address_info b where b.address = :address and a.tx_id = b.tx_hash order by block_height desc LIMIT :limit, :offset", nativeQuery = true)
    List<TxInfo> getTxInfos(@Param("address") String address, @Param("limit") Integer limit, @Param("offset") Integer offset);
}
