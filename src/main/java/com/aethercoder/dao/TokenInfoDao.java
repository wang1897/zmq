package com.aethercoder.dao;

import com.aethercoder.entity.TokenInfo;
import com.aethercoder.entity.TxInfo;
import org.springframework.data.jpa.repository.JpaRepository;
import org.springframework.data.jpa.repository.Query;
import org.springframework.stereotype.Repository;

import java.math.BigInteger;
import java.util.List;
import java.util.Map;

/**
 * Created by king on 20/09/2018.
 */
@Repository
public interface TokenInfoDao extends JpaRepository<TokenInfo, Long> {

    List<TokenInfo> getByIdNotNull();
}
