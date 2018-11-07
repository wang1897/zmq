package com.aethercoder.dao.impl;

import com.aethercoder.util.DateUtil;
import org.springframework.stereotype.Repository;

import javax.persistence.EntityManager;
import javax.persistence.PersistenceContext;
import javax.persistence.Query;
import java.math.BigDecimal;
import java.util.ArrayList;
import java.util.HashMap;
import java.util.List;
import java.util.Map;

/**
 * Created by king on 11/09/2018.
 */
@Repository
public class BlockInfoDaoImpl {
    @PersistenceContext
    private EntityManager entityManager;

    public List<Map> findByLatestBlockInfos(Integer limit, Integer offset) {
        String hql = "select * from t_block_info order by block_height desc";

        Query query = entityManager.createNativeQuery(hql);
        query.setFirstResult(offset);
        query.setMaxResults(limit);
        List searchRes = query.getResultList();
        List<Map> result = new ArrayList<Map>();
        for (int i = 0; i < searchRes.size(); i++) {
            Object[] objects = (Object[]) searchRes.get(i);
            int j = 0;

            Map blockMap = new HashMap();
            blockMap.put("blockHash", objects[1].toString());
            blockMap.put("blockHeight", Integer.valueOf(objects[2].toString()));
            blockMap.put("blockSize", Integer.valueOf(objects[3].toString()));
            blockMap.put("blockTime", DateUtil.stringToDate(objects[5].toString()));
            blockMap.put("blockAward", BigDecimal.valueOf(Double.valueOf(objects[6].toString())));
            blockMap.put("blockMiner", objects[8].toString());
            blockMap.put("blockTxcount", Integer.valueOf(objects[9].toString()));

            result.add(blockMap);
        }

        return result;
    }

    public Map findByBlockHeight(Integer blockHeight) {
        String hql = "select * from t_block_info where block_height = :blockHeight  order by block_height desc";

        Query query = entityManager.createNativeQuery(hql);
        query.setParameter("blockHeight", blockHeight);
        List searchRes = query.getResultList();

        return generateBlockInfo(searchRes);
    }

    public Map findByBlockHash(String blockHash) {
        String hql = "select * from t_block_info where block_hash = :blockHash  order by block_height desc";

        Query query = entityManager.createNativeQuery(hql);
        query.setParameter("blockHash", blockHash);
        List searchRes = query.getResultList();

        return generateBlockInfo(searchRes);
    }

    public Map generateBlockInfo(List searchRes){
        Map blockMap = new HashMap();
        for (int i = 0; i < searchRes.size(); i++) {
            Object[] objects = (Object[]) searchRes.get(i);
            int j = 0;

            blockMap.put("blockHash", objects[1].toString());
            blockMap.put("blockHeight", Integer.valueOf(objects[2].toString()));
            blockMap.put("blockWeight", Integer.valueOf(objects[4].toString()));
            blockMap.put("blockSize", Integer.valueOf(objects[3].toString()));
            blockMap.put("blockTime", DateUtil.stringToDate(objects[5].toString()));
            blockMap.put("blockAward", BigDecimal.valueOf(Double.valueOf(objects[6].toString())));
            blockMap.put("blockMerkle", objects[7].toString());
            blockMap.put("blockMiner", objects[8].toString());
            blockMap.put("blockTxcount", Integer.valueOf(objects[9].toString()));
            blockMap.put("blockPreblockhash", objects[10].toString());
        }

        return blockMap;
    }
}
