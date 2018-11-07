package com.aethercoder.service;

import com.aethercoder.dao.AddressInfoDao;
import com.aethercoder.dao.BlockInfoDao;
import com.aethercoder.dao.TxInfoDao;
import com.aethercoder.entity.AddressInfo;
import com.aethercoder.entity.BlockInfo;
import com.aethercoder.entity.TxInfo;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.stereotype.Service;

import javax.transaction.Transactional;
import java.util.List;

/**
 * Created by hepengfei on 23/01/2018.
 */
@Service
public class TransactionService {
    @Autowired
    private BlockInfoDao blockInfoDao;

    @Autowired
    private TxInfoDao txInfoDao;

    @Autowired
    private AddressInfoDao addressInfoDao;

    @Transactional
    public void save(BlockInfo block, List<TxInfo> txInfos, List<AddressInfo> addressInfos) throws Exception {
        blockInfoDao.save(block);
        txInfoDao.save(txInfos);
        addressInfoDao.save(addressInfos);
    }
}
