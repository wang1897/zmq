package com.aethercoder.dao.impl;

import com.aethercoder.util.DateUtil;
import org.springframework.stereotype.Repository;

import javax.persistence.EntityManager;
import javax.persistence.PersistenceContext;
import javax.persistence.Query;
import java.math.BigDecimal;
import java.util.*;

@Repository
public class TxInfoDaoImpl {
    @PersistenceContext
    private EntityManager entityManager;

    public Map getTxInfo(String txHash) {
        String hql = "select * from tx_info where tx_id = :txHash";

        Query query = entityManager.createNativeQuery(hql);
        query.setParameter("txHash", txHash);
        List searchRes = query.getResultList();
        Map txMap = new HashMap();
        for (int i = 0; i < searchRes.size(); i++) {
            Object[] objects = (Object[]) searchRes.get(i);
            int j = 0;

            txMap.put("txId", objects[1].toString());
            txMap.put("txVin", objects[3].toString());
            txMap.put("txVout", objects[5].toString());
            txMap.put("time", DateUtil.stringToDate(objects[6].toString()));
            txMap.put("blockHash", objects[7].toString());
            txMap.put("blockHeight", Integer.valueOf(objects[8].toString()));
            txMap.put("size", Integer.valueOf(objects[9].toString()));
            txMap.put("txFee", BigDecimal.valueOf(Double.valueOf(objects[10].toString())));
        }

        return txMap;
    }

    public List<Map> findByLatestTxInfos(Integer limit, Integer offset){
        String hql = "select * from tx_info order by time desc ";

        Query query = entityManager.createNativeQuery(hql);
        query.setMaxResults(limit);
        query.setFirstResult(offset);
        List searchRes = query.getResultList();

        List<Map> txInfos = new ArrayList<Map>();
        for (int i = 0; i < searchRes.size(); i++) {
            Object[] objects = (Object[]) searchRes.get(i);
            int j = 0;

            Map txMap = new HashMap();
            txMap.put("txId", objects[1].toString());
            txMap.put("txVin", objects[3].toString());
            txMap.put("txVout", objects[5].toString());
            txMap.put("time", DateUtil.stringToDate(objects[6].toString()));
            txMap.put("blockHash", objects[7].toString());
            txMap.put("blockHeight", Integer.valueOf(objects[8].toString()));
            txMap.put("size", Integer.valueOf(objects[9].toString()));
            txMap.put("txFee", BigDecimal.valueOf(Double.valueOf(objects[10].toString())));

            txInfos.add(txMap);
        }

        return txInfos;

    }
}
