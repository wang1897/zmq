package com.aethercoder.service;

import com.aethercoder.entity.AddressInfo;
import com.aethercoder.entity.BlockInfo;
import com.aethercoder.entity.TokenInfo;
import com.aethercoder.entity.TxInfo;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.beans.factory.annotation.Value;

import java.util.ArrayList;
import java.util.HashMap;
import java.util.List;
import java.util.Map;
import java.util.concurrent.BlockingDeque;

public class SynBlockService implements Runnable  {
    private Logger logger = LoggerFactory.getLogger(QueueSubService.class);

    private QtumService qtumService;

    private BlockingDeque<String> blockingDeque;

    private Integer blockHeight;

    public SynBlockService(QtumService qtumService, BlockingDeque<String> blockingDeque, Integer blockHeight){
        this.qtumService = qtumService;
        this.blockingDeque = blockingDeque;
        this.blockHeight = blockHeight;
    }

    @Override
    public void run() {
        try {
            // 查询数据库中所有的区块高度
            List<Integer> heightList = qtumService.getHeightList();

            int min = 1;
            for (int count: heightList) {
                for(int i = min; i < blockHeight; i++){
                    if (i < count) {
                        Thread.sleep(5000);
                        blockingDeque.put(qtumService.getBlockHash(Long.valueOf(i)));
                        logger.info("synMissingBlock, remaining block size: " + blockingDeque.size());
                    }
                    else {
                        min = count + 1;
                        break;
                    }
                }
            }

        }
        catch (Exception e) {
            e.printStackTrace();
        }
    }
}
