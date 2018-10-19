package com.aethercoder.controller;

import com.aethercoder.service.QtumService;
import com.aethercoder.vo.TransactionVO;
import com.google.gson.Gson;
import com.google.gson.reflect.TypeToken;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.web.bind.annotation.*;

import java.util.List;

/**
 * Created by hepengfei on 25/01/2018.
 */
@RestController
@RequestMapping("qtumRPC")
public class QtumController {
    private static Logger logger = LoggerFactory.getLogger(QtumController.class);

    @Autowired
    private QtumService qtumService;

    @RequestMapping( value = "/history/transaction/{limit}/{offset}", method = RequestMethod.GET)
    public List<TransactionVO> getHistory(@RequestParam String addresses, @RequestParam(required=false) Long startBlock, @RequestParam(required=false) Long endBlock,
                                          @RequestParam(required = false) String contractAddress, @PathVariable("limit") Integer limit, @PathVariable("offset") Integer offset)
            throws Exception {

        List<String> addressList = new Gson().fromJson(addresses, new TypeToken<List<String>>(){}.getType());
        logger.info("/history/transaction/{limit}/{offset}");
        logger.info("startBlock: " + startBlock);
        logger.info("endBlock: " + endBlock);
        return qtumService.getTransactionByAddresses(addressList, contractAddress, startBlock, endBlock, offset, limit);
    }

    @RequestMapping( value = "/syncMissingBlock", method = RequestMethod.GET)
    public void syncMissingBlock(@RequestParam Long blockHeight) throws Exception {
        String blockHash = qtumService.getBlockHash(blockHeight);
        if (blockHash != null) {
            qtumService.syncBlockByBlockHash(blockHash);
        }
    }

    @RequestMapping( value = "/blockInfos/{limit}/{offset}", method = RequestMethod.GET)
    public String getLatestBlockInfos(@PathVariable("limit") Integer limit, @PathVariable("offset") Integer offset) throws Exception {
        logger.info("/blockInfos/{limit}/{offset}");

        return qtumService.getLatestBlockInfos(limit, offset);
    }

    @RequestMapping( value = "/blockInfo", method = RequestMethod.GET)
    public String getBlockInfo(@RequestParam String blockHashOrBlockCount) throws Exception {
        logger.info("/blockInfo");

        return qtumService.getBlockInfo(blockHashOrBlockCount);
    }

    @RequestMapping( value = "/addressInfo/{limit}/{offset}", method = RequestMethod.GET)
    public String getAddressInfo(@RequestParam String address, @PathVariable("limit") Integer limit, @PathVariable("offset") Integer offset) throws Exception {
        logger.info("/addressInfo");

        return qtumService.getAddressInfo(address, limit, offset);
    }

    @RequestMapping( value = "/transactionInfo", method = RequestMethod.GET)
    public String getTransactionInfo(@RequestParam String txHash) throws Exception {
        logger.info("/transactionInfo");

        return qtumService.getTransactionInfo(txHash);
    }

    @RequestMapping( value = "/tokenBalance", method = RequestMethod.GET)
    public String getTokenBalance(@RequestParam String address) throws Exception {
        logger.info("/tokenBalance");

        return qtumService.getTokenBalance(address);
    }

    @RequestMapping( value = "/queryByParam", method = RequestMethod.GET)
    public String queryByParam(@RequestParam String param) throws Exception {
        logger.info("/queryByParam");

        return qtumService.queryByParam(param);
//        return qtumService.getBlockAndTx();
    }
}
