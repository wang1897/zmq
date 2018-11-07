package com.aethercoder.service;

import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.stereotype.Service;

import java.util.List;
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
                        String missBlockHash = qtumService.getBlockHash(Long.valueOf(i));
                        if (!blockingDeque.contains(missBlockHash)){
                            blockingDeque.put(missBlockHash);
                        }

                        logger.info("SynBlockService synMissingBlock, remaining block size: " + blockingDeque.size());
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
