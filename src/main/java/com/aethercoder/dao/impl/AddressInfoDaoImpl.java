package com.aethercoder.dao.impl;

import com.aethercoder.util.DateUtil;
import com.google.gson.Gson;
import org.springframework.stereotype.Repository;

import javax.persistence.EntityManager;
import javax.persistence.PersistenceContext;
import javax.persistence.Query;
import java.math.BigDecimal;
import java.util.ArrayList;
import java.util.HashMap;
import java.util.List;
import java.util.Map;

@Repository
public class AddressInfoDaoImpl {
    @PersistenceContext
    private EntityManager entityManager;

    public String getAddressInfo(String address, Integer limit, Integer offset) {
        String hql = "select * from t_address_info where address = :address order by block_height desc";

        Query query = entityManager.createNativeQuery(hql);
        query.setParameter("address", address);
        List searchRes = query.getResultList();

        Map resultMap = new HashMap();
        Double inBalance = 0d;
        Double outBalance = 0d;
        for (int i = 0; i < searchRes.size(); i++) {
            Object[] objects = (Object[]) searchRes.get(i);

            Double change = Double.valueOf(objects[4].toString());
            if (change > 0){
                inBalance += change;
            }
            else{
                outBalance += change;
            }

            resultMap.put("symbol", objects[5].toString());
        }

        resultMap.put("balance", outBalance + inBalance);
        resultMap.put("inBalance", inBalance);
        resultMap.put("outBalance", outBalance);

        hql = "select * from t_tx_info where tx_id in (select distinct tx_hash from t_address_info where address = :address) order by block_height desc";
        query = entityManager.createNativeQuery(hql);
        query.setParameter("address", address);
        query.setFirstResult(offset);
        query.setMaxResults(limit);
        searchRes = query.getResultList();
        List<Map> txInfos = new ArrayList<Map>();
        for (int i = 0; i < searchRes.size(); i++) {
            Object[] objects = (Object[]) searchRes.get(i);

            Map txMap = new HashMap();
            txMap.put("txId", objects[1].toString());
            txMap.put("txVin", objects[3].toString());
            txMap.put("txVout", objects[5].toString());
            txMap.put("time", DateUtil.stringToDate(objects[6].toString()));
            txMap.put("blockHeight", Integer.valueOf(objects[8].toString()));
            txMap.put("txFee", BigDecimal.valueOf(Double.valueOf(objects[10].toString())));

            txInfos.add(txMap);
        }

        resultMap.put("txInfo", txInfos);
        return new Gson().toJson(resultMap);
    }
}
