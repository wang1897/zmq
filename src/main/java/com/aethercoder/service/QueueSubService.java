package com.aethercoder.service;

import com.aethercoder.entity.*;
import jdk.nashorn.internal.parser.Token;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.beans.factory.annotation.Value;
import org.springframework.stereotype.Service;

import java.math.BigDecimal;
import java.util.*;
import java.util.concurrent.ArrayBlockingQueue;
import java.util.concurrent.BlockingDeque;
import java.util.concurrent.ThreadPoolExecutor;
import java.util.concurrent.TimeUnit;

/**
 * Created by hepengfei on 24/01/2018.
 */
@Service
public class QueueSubService implements Runnable {
    private Logger logger = LoggerFactory.getLogger(QueueSubService.class);

    @Autowired
    private TransactionService transactionService;

    @Autowired
    private QtumService qtumService;

    @Value("${qtum.zmq}")
    private String zmqUrl;

    private BlockingDeque<String> blockingDeque;

    public BlockingDeque<String> getBlockingDeque() {
        return blockingDeque;
    }

    public void setBlockingDeque(BlockingDeque<String> blockingDeque) {
        this.blockingDeque = blockingDeque;
    }

    /**
     * 执行逻辑
     */
    @Override
    public void run() {
        String blockHash = null;
        try {
            int blocks = 999;
            while (!Thread.currentThread().isInterrupted()) {


                blockHash = blockingDeque.take();
                logger.info("blockHash: " + blockHash);
                Map blockDetail = qtumService.getBlockDetail(blockHash);
                blocks++;

                List<Transaction> transactions = new ArrayList<>();
                List<AddressTx> addressTxList = new ArrayList<>();

                List<AddressInfo> addressInfoList = new ArrayList<AddressInfo>();
                List<TxInfo> txList = new ArrayList<TxInfo>();
                List<String> transList = (List<String>) blockDetail.get("tx");
                Map<String, Double> txFeeMap = new HashMap<String, Double>();
                for (String txHash : transList) {

                    //生成地址信息和交易信息
                    qtumService.generateAddressAndTxInfo(txHash,
                                                         addressInfoList,
                                                         txList,
                                                         (Integer)blockDetail.get("height"),
                                                         txFeeMap);

                    // address_tx和transaction表
                    Map transDetail = qtumService.getTransDetail(txHash);
                    Transaction entityTrans = qtumService.transferDetailToTransaction(transDetail, (Integer) blockDetail.get("height"));
                    qtumService.transferDetailToAddressTx(transDetail, txHash, addressTxList);

                    transactions.add(entityTrans);
                }


                transactionService.saveTrans(transactions, addressTxList);

                BlockInfo block = generateBlockInfo(blockDetail, txFeeMap.get("blockFee"), blockHash, transList.size());
                transactionService.save(block, txList, addressInfoList);

                //同步丢失的区块
                if (blocks % 100 == 0){
                    ThreadPoolExecutor executor = new ThreadPoolExecutor(10, 10, 200, TimeUnit.MILLISECONDS,
                            new ArrayBlockingQueue<Runnable>(20));
                    executor.execute(new SynBlockService(qtumService, blockingDeque, block.getBlockHeight()));
                }

                logger.info("remaining block size: " + blockingDeque.size());
            }
        }
        catch (Exception e) {
            e.printStackTrace();
            logger.error(blockHash + " " + e.getMessage(), e);
        }
    }

    private BlockInfo generateBlockInfo(Map blockDetail, Double blockAward, String blockHash, Integer txCount){
        BlockInfo block = qtumService.blockDetailToBlock(blockDetail);
        block.setBlockAward(BigDecimal.valueOf(blockAward));
        block.setBlockHash(blockHash);
        block.setBlockMerkle((String)blockDetail.get("merkleroot"));
        block.setBlockMiner((String) blockDetail.get("miner"));
        block.setBlockSize((Integer) blockDetail.get("size"));
        block.setBlockTime(new Date(new Long((Integer)blockDetail.get("time")) * 1000));
        block.setBlockTxcount(txCount);
        block.setBlockWeight((Integer)blockDetail.get("weight"));
        block.setBlockHeight((Integer)blockDetail.get("height"));
        block.setBlockPreblockhash((String)blockDetail.get("previousblockhash"));

        return block;
    }
}
