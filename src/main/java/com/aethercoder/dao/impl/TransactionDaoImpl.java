package com.aethercoder.dao.impl;

import com.aethercoder.entity.Transaction;
import com.aethercoder.util.DateUtil;
import org.springframework.stereotype.Repository;

import javax.persistence.EntityManager;
import javax.persistence.PersistenceContext;
import javax.persistence.Query;
import java.math.BigInteger;
import java.util.ArrayList;
import java.util.Date;
import java.util.List;

/**
 * Created by hepengfei on 25/01/2018.
 */
@Repository
public class TransactionDaoImpl {
    @PersistenceContext
    private EntityManager entityManager;

    public List<Transaction> findByAddress(List<String> addressList, String contractAddress, Long startBlock, Long endBlock , Integer offset, Integer limit) {
        /* sample sql
        select tx.*
from transaction tx inner join address_tx ad on tx.tx_id = ad.tx_hash
where ad.address in ('QNUH4qEBs5AhVjy9figZDwH8M8wwQEpern')
and ad.token_addr = 'QgNgvQpA1tbr9LV3nSTT5Xaxu1k5aHsZAH'
union
select t.* from address_tx tx1, address_tx tx2, transaction t
where tx1.tx_hash = tx2.tx_hash
and tx2.tx_hash = t.tx_id
and tx2.in_out=1
and tx2.address='QNUH4qEBs5AhVjy9figZDwH8M8wwQEpern'
and tx1.token_addr = 'QgNgvQpA1tbr9LV3nSTT5Xaxu1k5aHsZAH'
group by tx_id
having 1=1
order by if(isnull(block_height),0,1) , block_height desc;
         */
//        String hql = "select tx from Transaction tx, AddressTx ad where tx.txId = ad.txHash and ad.address in (:addressList)";
        String hql = "select tx.* from transaction tx, address_tx ad where tx.tx_id = ad.tx_hash and ad.address in (:addressList)";
        if (contractAddress != null) {
            hql += " and ad.token_addr = :contractAddress and ad.is_token = 1";
        }
        if (contractAddress != null) {
            hql += " union select tx.* from transaction tx, address_tx ad1, address_tx ad2 where tx.tx_id = ad1.tx_hash and ad1.tx_hash = ad2.tx_hash" +
                    " and ad2.in_out = 1 and ad2.address in (:addressList) and ad1.token_addr = :contractAddress and ad2.is_token=1";
        }
        hql += " group by tx_Id having 1=1";
        if (startBlock != null) {
            hql += " and block_height >= :startBlock";
        }
        if (endBlock != null) {
            hql += " and block_height <= :endBlock";
        }
        hql += " order by if(isnull(block_height),0,1), block_height desc";

        Query query = entityManager.createNativeQuery(hql);
        query.setParameter("addressList", addressList);
        if (contractAddress != null) {
            query.setParameter("contractAddress", contractAddress);
        }
        if (startBlock != null) {
            query.setParameter("startBlock", startBlock);
        }
        if (endBlock != null) {
            query.setParameter("endBlock", endBlock);
        }
        query.setFirstResult(offset);
        query.setMaxResults(limit);
        List searchRes = query.getResultList();
        List<Transaction> result = new ArrayList<>();
        for (int i = 0; i < searchRes.size(); i++) {
            Object[] objects = (Object[])searchRes.get(i);
            int j = 0;
            Object o = null;
            Transaction transaction = new Transaction();
            transaction.setTxId(objects[j++].toString());
            transaction.setTxRaw(objects[j++].toString());
            o = objects[j++];
            if (o != null) {
                transaction.setTime(DateUtil.stringToDate(o.toString()));
            }
            o = objects[j++];
            if (o != null) {
                transaction.setBlockHash(o.toString());
            }
            o = objects[j++];
            if (o != null) {
                transaction.setBlockHeight(Long.valueOf(o.toString()));
            }
            transaction.setSize((Integer)objects[j++]);
            transaction.setLocktime((Integer)objects[j++]);
            transaction.setId(Long.valueOf(objects[j++].toString()));
            result.add(transaction);
        }
        return result;
    }


    public Long findMinUnloggedHeight() {
        /*select min(tx.block_height) from transaction tx, address_tx ad where tx.tx_id = ad.tx_hash and ad.is_token = 1 and token_logged = 0;*/
        String hql = "select min(tx.block_height) from transaction tx, address_tx ad where tx.tx_id = ad.tx_hash and ad.is_token = 1 and token_logged = 0;";

        Query query = entityManager.createNativeQuery(hql);
        Object object = query.getSingleResult();
        Long result = null;
        if (object != null) {
            result = Long.valueOf(object.toString());
        }
        return result;
    }
}
