package com.aethercoder.dao;

import org.springframework.data.jpa.repository.JpaRepository;
import com.aethercoder.entity.AddressTx;
import org.springframework.stereotype.Repository;

import java.util.List;

/**
 * Created by hepengfei on 23/01/2018.
 */
@Repository
public interface AddressTxDao extends JpaRepository<AddressTx, Long> {
    List<AddressTx> findByAddress(String address);

    List<AddressTx> findByTxHash(String txHash);

    List<AddressTx> findByTxHashInAndIsTokenAndTokenLogged(List<String> txHash, boolean isToken, boolean tokenLogged);
}
