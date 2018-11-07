package com.aethercoder.dao.impl;

import com.aethercoder.entity.TokenInfo;
import org.springframework.stereotype.Repository;

import javax.persistence.EntityManager;
import javax.persistence.PersistenceContext;
import javax.persistence.Query;
import java.util.HashMap;
import java.util.List;
import java.util.Map;

@Repository
public class TokenInfoDaoImpl {
    @PersistenceContext
    private EntityManager entityManager;

    public Map<String, TokenInfo> getTokenInfoMap() {
        String hql = "select * from t_token_info ";

        Query query = entityManager.createNativeQuery(hql);
        List searchRes = query.getResultList();
        Map<String, TokenInfo> resultMap = new HashMap<String, TokenInfo>();
        for (int i = 0; i < searchRes.size(); i++) {
            Object[] objects = (Object[]) searchRes.get(i);
            int j = 0;

            TokenInfo tokenInfo = new TokenInfo();
            tokenInfo.setSymbol(objects[1].toString());
            tokenInfo.setSymbol(objects[2].toString());
            tokenInfo.setContractAddress(objects[3].toString());
            tokenInfo.setDecimal(Integer.valueOf(objects[4].toString()));
            resultMap.put(tokenInfo.getContractAddress(), tokenInfo);
        }

        return resultMap;
    }
}
