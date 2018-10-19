package com.aethercoder.service;

import com.aethercoder.dao.*;
import com.aethercoder.entity.*;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.stereotype.Service;

import javax.transaction.Transactional;
import java.util.ArrayList;
import java.util.HashMap;
import java.util.List;
import java.util.Map;

/**
 * Created by hepengfei on 23/01/2018.
 */
@Service
public class TransactionService {
    @Autowired
    private AddressTxDao addressTxDao;

    @Autowired
    private TransactionDao transactionDao;

    @Autowired
    private BlockInfoDao blockInfoDao;

    @Autowired
    private TxInfoDao txInfoDao;

    @Autowired
    private AddressInfoDao addressInfoDao;

    @Autowired
    private QtumService qtumService;

    @Transactional
    public void saveTrans( List<Transaction>  transactions, List<AddressTx> addressTxList) throws Exception {
        transactionDao.save(transactions);
        addressTxDao.save(addressTxList);
    }

    @Transactional
    public void deleteTrans(Transaction transaction, List<AddressTx> addressTxList) throws Exception {
        transactionDao.delete(transaction);
        addressTxDao.delete(addressTxList);
    }

    public List<Object> getUnconfirmedTx() throws Exception {
        Map chainInfo = qtumService.getInfo();
        Long blocks = Long.valueOf(chainInfo.get("blocks").toString());

        long unconfirmedBlock = blocks - 5;

        List<Object> list = new ArrayList<>();
        List<Transaction> txList = transactionDao.findByTimeIsNullOrBlockHeightAfter(unconfirmedBlock);
        Map<String, List<AddressTx>> map = new HashMap<>();
        if (txList != null && !txList.isEmpty()) {
            for(Transaction transaction: txList) {
                List<AddressTx> addressTxList = addressTxDao.findByTxHash(transaction.getTxId());
                map.put(transaction.getTxId(), addressTxList);
            }
            list.add(txList);
            list.add(map);
        }
        return list;
    }

    @Transactional
    public void save(BlockInfo block, List<TxInfo> txInfos, List<AddressInfo> addressInfos) throws Exception {
        blockInfoDao.save(block);
        txInfoDao.save(txInfos);
        addressInfoDao.save(addressInfos);
    }
}
