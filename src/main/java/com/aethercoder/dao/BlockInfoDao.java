package com.aethercoder.dao;

import com.aethercoder.entity.BlockInfo;
import org.springframework.data.jpa.repository.JpaRepository;
import org.springframework.data.jpa.repository.Query;
import org.springframework.data.repository.query.Param;
import org.springframework.stereotype.Repository;

import java.util.List;

/**
 * Created by king on 11/09/2018.
 */
@Repository
public interface BlockInfoDao extends JpaRepository<BlockInfo, Long>{

    @Query(value = "select max(block_height) from t_block_info", nativeQuery = true)
    Long findMaxBlockHeight();

    @Query(value = "select DISTINCT(block_height) from t_block_info ORDER BY block_height asc", nativeQuery = true)
    List<Integer> getAllBlockHeightFromDB();

    @Query(value = "select * from t_block_info order by block_height desc LIMIT :limit, :offset",nativeQuery = true)
    List<BlockInfo> getBypage(@Param("limit") Integer limit, @Param("offset") Integer offset);

    BlockInfo getByBlockHash(String blockHash);

    BlockInfo getByBlockHeight(Integer blockHeight);
}
