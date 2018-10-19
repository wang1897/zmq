package com.aethercoder.service;

import com.aethercoder.dao.AddressTxDao;
import com.aethercoder.dao.TransactionDao;
import com.aethercoder.dao.impl.TransactionDaoImpl;
import com.aethercoder.entity.AddressTx;
import com.aethercoder.entity.Transaction;
import com.aethercoder.util.CurrentNetParam;
import com.aethercoder.util.QtumUtils;
import com.aethercoder.vo.TokenTransfer;
import org.apache.tomcat.util.buf.HexUtils;
import org.bitcoinj.core.Address;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.stereotype.Service;

import java.util.ArrayList;
import java.util.List;
import java.util.Map;

/**
 * Created by hepengfei on 01/02/2018.
 */
@Service
public class TokenLogService implements Runnable{
    private Logger logger = LoggerFactory.getLogger(TokenLogService.class);
    @Autowired
    private TransactionDaoImpl transactionDaoImpl;

    @Autowired
    private TransactionDao transactionDao;

    @Autowired
    private AddressTxDao addressTxDao;

    @Autowired
    private QtumService qtumService;

    private int longSleepTime = 30000;


    @Override
    public void run() {
        while(true) {
            try {
                Long blockHeight = transactionDaoImpl.findMinUnloggedHeight();
                if (blockHeight == null) {
                    try {
                        Thread.sleep(longSleepTime);
                        continue;
                    } catch (InterruptedException ie) {

                    }
                }
                List<Transaction> transactionList = transactionDao.findByBlockHeight(blockHeight);
                if (transactionList == null || transactionList.isEmpty()) {
                    continue;
                }
                List<String> txHashList = new ArrayList<>();
                List<String> contractAddressList = new ArrayList<>();
                for (Transaction transaction : transactionList) {
                    txHashList.add(transaction.getTxId());
                }
                List<AddressTx> addressTxList = addressTxDao.findByTxHashInAndIsTokenAndTokenLogged(txHashList, true, false);
                for (AddressTx addressTx : addressTxList) {
                    if (!addressTx.getIn() && addressTx.getTokenAddr() != null) {
                        String addr160 = addressTx.getTokenAddrSha160();
                        if (!contractAddressList.contains(addr160)) {
                            contractAddressList.add(addressTx.getTokenAddrSha160());
                        }
                    }
                }
                logger.info("thread id: " + Thread.currentThread().getId());
                List eventLogList = qtumService.getEventLog(blockHeight, blockHeight, contractAddressList);
                for (AddressTx addressTx: addressTxList) {
                    String txHash = addressTx.getTxHash();
                    String fromAddress = null;
                    String contractAddress = null;
                    for (int i = 0; i < eventLogList.size(); i++) {
                        Map eventLogMap = (Map)eventLogList.get(i);
                        if (txHash.equals(eventLogMap.get("transactionHash").toString())) {
                            fromAddress = eventLogMap.get("from").toString();
                            contractAddress = eventLogMap.get("contractAddress").toString();
                            Address address = new Address(CurrentNetParam.get(), HexUtils.fromHexString(fromAddress));
                            fromAddress = address.toBase58();
                            break;
                        }
                    }
                    if (fromAddress != null) {
                        String asm = addressTx.getAsm();
                        String[] asmBody = asm.split(" ");
                        if (asmBody.length > 3) {
                            try {
                                TokenTransfer tokenTransfer = new TokenTransfer(asmBody[3]);
                                addressTx.setToken(true);
                                addressTx.setAddress(tokenTransfer.getToAddress());
                                addressTx.setTokenAmount(tokenTransfer.getValue().toString());
                                addressTx.setTokenAddr(QtumUtils.sha160ToBase58(contractAddress));
                                addressTx.setTokenFromAddr(fromAddress);
                            } catch (RuntimeException re) {
                                logger.warn("not a valid token transfer: " + addressTx.getTxHash());
                            }
                        }

                        addressTx.setTokenLogged(true);
                    } else {
                        if (addressTx.getTokenAmount() != null) {
                            addressTx.setAddress(addressTx.getTokenAddr());
                        }
                        addressTx.setToken(false);
                        addressTx.setTokenAmount(null);
                        addressTx.setTokenAddr(null);
                        addressTx.setTokenLogged(true);
                    }
                }
                addressTxDao.save(addressTxList);
            } catch (Exception e) {
                logger.error("error when process token event log", e);
            }
        }
    }
}
