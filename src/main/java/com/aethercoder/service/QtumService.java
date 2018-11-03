package com.aethercoder.service;

import com.aethercoder.dao.AddressTxDao;
import com.aethercoder.dao.BlockInfoDao;
import com.aethercoder.dao.TransactionDao;
import com.aethercoder.dao.impl.*;
import com.aethercoder.entity.*;
import com.aethercoder.util.BeanUtils;
import com.aethercoder.util.CurrentNetParam;
import com.aethercoder.util.NetworkUtil;
import com.aethercoder.util.QtumUtils;
import com.aethercoder.vo.TokenTransfer;
import com.aethercoder.vo.TransactionVO;
import com.fasterxml.jackson.core.JsonProcessingException;
import com.fasterxml.jackson.databind.ObjectMapper;
import com.google.gson.Gson;
import org.apache.tomcat.util.buf.HexUtils;
import org.bitcoinj.core.Address;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.beans.factory.annotation.Value;
import org.springframework.http.HttpMethod;
import org.springframework.stereotype.Service;

import java.math.BigDecimal;
import java.math.BigInteger;
import java.util.*;
import java.util.regex.Pattern;

/**
 * Created by hepengfei on 23/01/2018.
 */
@Service
public class QtumService {

    private Logger logger = LoggerFactory.getLogger(QtumService.class);

    private Gson gson = new Gson();

    @Value("${qtum.username}")
    private String username;

    @Value("${qtum.password}")
    private String password;

    @Value("${qtum.url}")
    private String qtumUrl;

    @Value("${qtum.event_url}")
    private String eventUrl;

    @Autowired
    private TransactionService transactionService;

    @Autowired
    private TransactionDaoImpl transactionDaoImpl;

    @Autowired
    private AddressTxDao addressTxDao;

    @Autowired
    private TransactionDao transactionDao;

    @Autowired
    private BlockInfoDaoImpl blockInfoDaoImpl;

    @Autowired
    private BlockInfoDao blockInfoDao;

    @Autowired
    private TxInfoDaoImpl txInfoDaoImpl;

    @Autowired
    private AddressInfoDaoImpl addressInfoDaoImpl;

    @Autowired
    private TokenInfoDaoImpl tokenInfoDaoImpl;

    public String getTransactionJson(String txid) throws JsonProcessingException {
        Map map = new HashMap();
        map.put("method", "getrawtransaction");

        List<Object> params = new ArrayList<>();
        params.add(txid);
        params.add(1);
        map.put("params", params);

        ObjectMapper mapper = new ObjectMapper();
        String json = mapper.writeValueAsString(map);
        return json;
    }

    public Integer getBlockCount() throws Exception{
        List<Map<String, Object>> paramList = new ArrayList<>();
        Integer blockCount = (Integer) callQtumService("getblockcount",paramList);
        return blockCount;
    }

    public HashMap getTransDetail(String txhash) throws Exception{
        List txidParamList = new ArrayList();
        txidParamList.add(txhash);
        txidParamList.add(true);

        HashMap rawTraction = (HashMap)callQtumService("getrawtransaction", txidParamList);

        return rawTraction;
    }

    public HashMap getBlockDetail(String blockHash) throws Exception{
        List blockHashParamList = new ArrayList();
        blockHashParamList.add(blockHash);

        HashMap blockInfo = (HashMap)callQtumService("getblock", blockHashParamList);

        return blockInfo;
    }

    public String getBlockHash(Long blockHeigth) throws Exception {
        List txidParamList = new ArrayList();
        txidParamList.add(blockHeigth);
        String blockHash = (String)callQtumService("getblockhash", txidParamList);
        return blockHash;
    }

    public List getEventLog(Long blockStart, Long blockEnd, List<String> contractAddrList) throws Exception{
        Map<String, List> addressMap = new HashMap<>();
        addressMap.put("addresses", contractAddrList);

        List<Object> paramsList = new ArrayList<>();
        paramsList.add(blockStart);
        paramsList.add(blockEnd);
        paramsList.add(addressMap);
        logger.info("start searchlogs " + paramsList);
        List result = (List)this.callQtumService("searchlogs", paramsList);
        logger.info("end searchlogs " + result);
        return result;
    }

    public HashMap getBlock(String blockHash) throws Exception {
        List txidParamList = new ArrayList();
        txidParamList.add(blockHash);

        HashMap rawTraction = (HashMap)callQtumService("getblock", txidParamList);
        return rawTraction;
    }

    public void syncBlockByBlockHash(String blockHash) throws Exception{
        HashMap blockMap = getBlock(blockHash);
        if (blockMap == null || blockMap.isEmpty() || blockMap.get("tx") == null) {
            return;
        }

        List<Transaction> transactions = new ArrayList<>();
        List<AddressTx> addressTxList = new ArrayList<>();
        List txIds = (List)blockMap.get("tx");
        for (Object o : txIds) {
            String txHash = (String)o;
            Map transDetail = getTransDetail(txHash);

            transactions.add(transferDetailToTransaction(transDetail,  (Integer) blockMap.get("height")));
            transferDetailToAddressTx(transDetail, txHash, addressTxList);
        }

        transactionService.saveTrans(transactions, addressTxList);
    }

    public Transaction transferDetailToTransaction(Map transDetail, Integer blockHeight) {
        Transaction transaction = new Transaction();
        transaction.setTxId((String)transDetail.get("txid"));
        transaction.setTxRaw((String)transDetail.get("hex"));
        transaction.setSize((Integer)transDetail.get("size"));
        transaction.setLocktime((Integer)transDetail.get("locktime"));
        transaction.setBlockHeight(Long.valueOf(blockHeight));
        if (transDetail.get("blockhash") != null) transaction.setBlockHash((String)transDetail.get("blockhash"));
//        if (transDetail.get("height") != null) transaction.setBlockHeight(Long.valueOf(transDetail.get("height").toString()));
        if (transDetail.get("time") != null) {
            Integer timestamp = (Integer)transDetail.get("time");
            Date date = new Date(new Long(timestamp) * 1000);
            transaction.setTime(date);
        }
        return transaction;
    }

    public BlockInfo blockDetailToBlock(Map blockDetail){
        BlockInfo block = new BlockInfo();
        block.setBlockHeight(Integer.valueOf(blockDetail.get("height").toString()));
        block.setBlockHash((String)blockDetail.get("hash"));
        block.setBlockMerkle((String)blockDetail.get("merkleroot"));
        block.setBlockMiner((String)blockDetail.get("miner"));
        block.setBlockPreblockhash((String)blockDetail.get("previousblockhash"));
        block.setBlockSize((Integer)blockDetail.get("size"));
        block.setBlockTime(new Date(new Long((Integer)blockDetail.get("time") * 1000)));
        block.setBlockWeight((Integer)blockDetail.get("weight"));

        return block;
    }

    public void generateAddressAndTxInfo(String txid,
                                         List<AddressInfo> addressInfoList,
                                         List<TxInfo> txInfos,
                                         Integer height,
                                         Map<String, Double> txFeeMap) throws Exception{
        Map transDetail = getTransDetail(txid);
        List<Map> vin = (List)transDetail.get("vin");

        //地址余额变动的集合
        Map<String, AddressInfo> addressValueMap = new HashMap<String, AddressInfo>();
        List<Map> txVin = new ArrayList<>();
        List<Map> txVout = new ArrayList<>();
        boolean isBaseFlag = false;
        Double txFee = 0d;

        // 处理交易输入（挖矿交易、合约输入）
        for (Map map: vin){
            if (map.get("coinbase") != null){
                Map txVinMap = new HashMap();
                txVinMap.put("address", "Coinbase");
                txVinMap.put("value", 0);
                txVin.add(txVinMap);
                isBaseFlag = true;
                continue;
            }

            List<Map> vinTxMap = (List)getTransDetail((String) map.get("txid")).get("vout");
            Map scriptPubKeyMap = null;
            for (Map vinTxMapPer : vinTxMap) {
                if (((Integer)map.get("vout")) == (Integer) vinTxMapPer.get("n")){
                    String address = "";
                    scriptPubKeyMap = (Map)vinTxMapPer.get("scriptPubKey");

                    //如果是合约调用 (合约调用 和 普通转账)
                    if("call".equalsIgnoreCase((String)scriptPubKeyMap.get("type"))){
                        address = ((String) scriptPubKeyMap.get("asm")).split(" ")[4];
                    }
                    else {
                        address = (String) ((List)scriptPubKeyMap.get("addresses")).get(0);
                    }

                    txFee += (Double)vinTxMapPer.get("value");
                    if (addressValueMap.containsKey(address)){
                        AddressInfo addressInfo = addressValueMap.get(address);
                        addressInfo.setBalance_change(addressInfo.getBalance_change().add(BigDecimal.valueOf(0 - (double)vinTxMapPer.get("value"))));
                    }
                    else{
                        //生成输入地址的信息
                        AddressInfo addressInfo = generateAddressInfo(addressInfoList,
                                                                      address,
                                                                      new BigDecimal(0d - (double)vinTxMapPer.get("value")),
                                                                      txid,
                                                                      (Integer)transDetail.get("time"),
                                                                      height,
                                                                     "QTC",
                                                                     null);

                        addressValueMap.put(address, addressInfo);
                    }

                    Map txVinMap = new HashMap();
                    txVinMap.put("address", address);
                    txVinMap.put("value", vinTxMapPer.get("value"));
                    txVin.add(txVinMap);
                }
            }
        }

        // 处理交易输出
        handleVoutMap(txFee, transDetail, height, txVout, isBaseFlag, addressInfoList, txFeeMap, addressValueMap);

        txFee = isBaseFlag ? txFeeMap.get("blockFee") : txFee;

        // 生成transaction信息
        generateTxInfo(transDetail, txInfos, BigDecimal.valueOf(txFee), gson.toJson(txVin), gson.toJson(txVout), vin.size(), ((List)transDetail.get("vout")).size(), height);
    }

    private Double handleVoutMap(Double txFee,
                                 Map transDetail,
                                 Integer height,
                                 List<Map> txVout,
                                 boolean isBaseFlag,
                                 List<AddressInfo> addressInfoList,
                                 Map<String, Double> txFeeMap,
                                 Map<String, AddressInfo> addressValueMap) throws Exception{
        List<Map> vout = (List)transDetail.get("vout");

        String address = "";
        for (Map map: vout){
            Map scriptPubKey = (Map) map.get("scriptPubKey");

            //如果是合约调用 (合约调用 和 普通转账、挖矿)
            if("call".equalsIgnoreCase((String)scriptPubKey.get("type"))){
                String[] asmBody = ((String) scriptPubKey.get("asm")).split(" ");
                if (asmBody.length > 3) {
                    address = asmBody[4];

                    if (asmBody[3].startsWith("a9059cbb")){
                        List<String> contractAddressList = new ArrayList<>();
                        contractAddressList.add(address);
                        List eventLogList = getEventLog(Long.valueOf(height), Long.valueOf(height), contractAddressList);
                        if (eventLogList.size() > 0)
                        {
                            Map<String, TokenInfo> tokenInfoMap = getTokenIndoMap();
                            if (tokenInfoMap.containsKey(address)){
                                TokenTransfer tokenTransfer = new TokenTransfer(asmBody[3]);

                                BigDecimal value = new BigDecimal(tokenTransfer.getValue()).divide(new BigDecimal(Math.pow(10, tokenInfoMap.get(address).getDecimal())));

                                // 生成代币交易的VoutMap
                                setVinAndVout(address, value, tokenInfoMap.get(address).getSymbol(), scriptPubKey, txVout);

                                //如果输出地址不在输入列表里面，则为新的地址生成余额变动信息
                                generateAddressInfo(addressInfoList,
                                        tokenTransfer.getToAddress(),
                                        value,
                                        (String) transDetail.get("txid"),
                                        (Integer)transDetail.get("time"),
                                        height,
                                        tokenInfoMap.get(address).getSymbol(),
                                        address);
                            }
                        }
                    }
                }
            }
            else {
                address = scriptPubKey.containsKey("addresses") ? (String) ((List)(scriptPubKey.get("addresses"))).get(0) : "nulldata";
            }

            // 如果是挖矿交易，则输出序号为0的value则为区块奖励
            if(isBaseFlag && 0 == (Integer)map.get("n")){
                txFeeMap.put("blockFee", (double)map.get("value"));
            }
            else if(isBaseFlag && 0 != (Integer)map.get("n")){
                txFeeMap.put(address, (double)map.get("value"));
            }

            //如果输出地址不在输入列表里面，则为新的地址生成余额变动信息
            if (addressValueMap.containsKey(address)){
                AddressInfo addressInfo = addressValueMap.get(address);
                addressInfo.setBalance_change(addressInfo.getBalance_change().add(BigDecimal.valueOf((double)map.get("value"))));
            }
            else{
                generateAddressInfo(addressInfoList,
                        address,
                        new BigDecimal((double)map.get("value")),
                        (String) transDetail.get("txid"),
                        (Integer)transDetail.get("time"),
                        height,
                        "QTC",
                        null);
            }

            // 生成交易的VoutMap
            setVinAndVout(address, new BigDecimal((double)map.get("value")), "QTC", scriptPubKey, txVout);

            // 计算交易的手续费时 扣除 花费返回部分
            txFee -= (double)map.get("value") + (txFeeMap.get(address) == null ? 0 : txFeeMap.get(address));
        }

        return txFee;
    }
    private void setVinAndVout(String address, BigDecimal value, String symbol, Map scriptPubKey, List<Map> vinOrVout){
        Map txVinOrVoutMap = new HashMap();
        txVinOrVoutMap.put("address", address);
        txVinOrVoutMap.put("value", value);
        txVinOrVoutMap.put("symbol", symbol);
        txVinOrVoutMap.put("Type", scriptPubKey.get("type"));
        txVinOrVoutMap.put("asm", scriptPubKey.get("asm"));
        vinOrVout.add(txVinOrVoutMap);
    }

    public void generateTxInfo(Map transDetail, List<TxInfo> txInfos, BigDecimal txFee, String txVin, String txVout, Integer vin, Integer vout, Integer height){
        TxInfo txInfo = new TxInfo();
        txInfo.setBlockHash((String)transDetail.get("blockhash"));
        txInfo.setSize((Integer) transDetail.get("size"));
        txInfo.setTime(new Date(new Long((Integer)transDetail.get("time")) * 1000));
        txInfo.setTxId((String)transDetail.get("txid"));
        txInfo.setTxVincount(vin);
        txInfo.setTxVin(txVin);
        txInfo.setTxVoutcout(vout);
        txInfo.setTxVout(txVout);
        txInfo.setTxFee(txFee);
        txInfo.setBlockHeight(height);
        txInfos.add(txInfo);
    }

    public AddressInfo generateAddressInfo(List<AddressInfo> addressInfoList,
                                           String addrress,
                                           BigDecimal change,
                                           String txid,
                                           Integer time,
                                           Integer height,
                                           String symbol,
                                           String contractAddress){
        AddressInfo addressInfo = new AddressInfo();
        addressInfo.setAddress(addrress);
        addressInfo.setBalance_change(change.setScale(8, BigDecimal.ROUND_HALF_UP));
        addressInfo.setTx_hash(txid);
        addressInfo.setBlockHeight(height);
        addressInfo.setTime(new Date(new Long(time) * 1000));
        addressInfo.setTokenSymbol(symbol);
        addressInfo.setTokenAddress(contractAddress);
        addressInfoList.add(addressInfo);

        return addressInfo;
    }

    public void transferDetailToAddressTx(Map transDetail, String txid, List<AddressTx> addressTxList) {
        List<Map> vin = (List)transDetail.get("vin");
        List<Map> vout = (List)transDetail.get("vout");
        String tokenAddress = null;
        boolean isToken = false;
        for (Map map: vout) {
            AddressTx addressTx = new AddressTx();
            boolean tempToken = transferVout(map, txid, addressTx);
            if (tempToken) {
                isToken = true;
                tokenAddress = addressTx.getTokenAddr();
            }
            addressTxList.add(addressTx);
        }
        if (isToken) {
            for (AddressTx addressTx : addressTxList) {
                addressTx.setToken(true);
            }
        }
        for (Map map: vin) {
            AddressTx addressTx = new AddressTx();
            if(map.get("coinbase") != null){
                addressTx.setAddress("coinbase");
                addressTx.setIndex(0);
            }

            if (isToken) {
                addressTx.setToken(true);
                addressTx.setTokenAddr(tokenAddress);
            }
            transferVin(map, txid, addressTx);
            addressTxList.add(addressTx);
        }
    }

    public void transferVin(Map vin, String txid, AddressTx addressTx) {
        addressTx.setTxHash(txid);
        if (vin.get("address") != null) {
            addressTx.setAddress((String)vin.get("address"));
        }
        if (vin.get("value") != null) {
            addressTx.setAmount(BigDecimal.valueOf((Double)vin.get("value")));
        }
        if (vin.get("txid") != null) {
            addressTx.setSpentTxId((String)vin.get("txid"));
        }
        if (vin.get("vout") != null) {
            addressTx.setIndex((Integer)vin.get("vout"));
        }
        Map scriptSig = (Map)vin.get("scriptSig");
        if (scriptSig != null) {
            if (scriptSig.get("asm") != null) {
                addressTx.setAsm((String)scriptSig.get("asm"));
            }
            if (scriptSig.get("hex") != null) {
                addressTx.setHex((String)scriptSig.get("hex"));
            }
        }
        addressTx.setIn(true);
    }

    public boolean transferVout(Map vout, String txid, AddressTx addressTx) {
        addressTx.setTxHash(txid);
        boolean isToken = false;
        TokenTransfer tokenTransfer = null;
        if (vout.get("value") != null) {
            addressTx.setAmount(BigDecimal.valueOf((Double)vout.get("value")));
        }
        if (vout.get("n") != null) {
            addressTx.setIndex((Integer)vout.get("n"));
        }
        if (vout.get("spentTxId") != null) {
            addressTx.setSpentTxId((String)vout.get("spentTxId"));
        }
        if (vout.get("spentIndex") != null) {
            addressTx.setSpentIndex((Integer)vout.get("spentIndex"));
        }
        if (vout.get("spentHeight") != null) {
            addressTx.setSpentHeight(Long.valueOf(vout.get("spentHeight").toString()));
        }
        Map scriptPubKey = (Map)vout.get("scriptPubKey");
        if (scriptPubKey != null) {
            if (scriptPubKey.get("asm") != null) {
                String asm = (String)scriptPubKey.get("asm");
                addressTx.setAsm(asm);
                String[] asmBody = asm.split(" ");
                if (asmBody.length > 3) {
                    String tokenRaw = asmBody[3];
                    try {
                        tokenTransfer = new TokenTransfer(tokenRaw);
                        addressTx.setToken(true);
                        addressTx.setAddress(tokenTransfer.getToAddress());
                        addressTx.setTokenAmount(tokenTransfer.getValue().toString());
                        isToken = true;
                    } catch (RuntimeException e) {

                    }
                }
            }
            if (scriptPubKey.get("hex") != null) {
                addressTx.setHex((String)scriptPubKey.get("hex"));
            }
            if (scriptPubKey.get("addresses") != null) {
                List<String> addressList = (List<String>)scriptPubKey.get("addresses");
                if (!addressList.isEmpty()) {
                    if (isToken) {
                        addressTx.setTokenAddr(addressList.get(0));
                    } else {
                        addressTx.setAddress(addressList.get(0));
                    }
                }
            }
        }
        addressTx.setIn(false);
        return isToken;
    }

//    public void syncUnconfimedTx() throws Exception{
////        logger.info("start getUnconfirmedTx");
//        List<Object> allList = transactionService.getUnconfirmedTx();
////        logger.info("end getUnconfirmedTx");
//        if (allList.isEmpty()) {
//            return;
//        }
//        List<Transaction> txList = (List<Transaction>)allList.get(0);
//        Map<String, List<AddressTx>> map = (Map)allList.get(1);
//
//        if (txList == null | txList.isEmpty()) {
//            return;
//        }
//        for (Transaction transaction: txList) {
//            try {
////                logger.info("start getTransDetail");
//                Map txDetail = getTransDetail(transaction.getTxId());
////                logger.info("end getTransDetail");
//                if (txDetail.get("height") == null) {
//                    continue;
//                } else if (Long.valueOf(txDetail.get("height").toString()) == -1) {
//                    List<AddressTx> pAddressTxList = map.get(transaction.getTxId());
//                    transactionService.deleteTrans(transaction, pAddressTxList);
//                    continue;
//                }
//
//                Integer timestamp = (Integer) txDetail.get("time");
//                Date date = new Date(new Long(timestamp) * 1000);
//                transaction.setTime(date);
//
//                if (txDetail.get("blockhash") != null) transaction.setBlockHash((String) txDetail.get("blockhash"));
//                if (txDetail.get("height") != null)
//                    transaction.setBlockHeight(Long.valueOf(txDetail.get("height").toString()));
//
//                List<AddressTx> pAddressTxList = map.get(transaction.getTxId());
//                List<AddressTx> addressTxList = transferDetailToAddressTx(txDetail, transaction.getTxId());
//                for (AddressTx pAddressTx : pAddressTxList) {
//                    int i = addressTxList.indexOf(pAddressTx);
//                    if (i >= 0) {
//                        AddressTx addressTx = addressTxList.get(i);
//                        pAddressTx.setSpentTxId(addressTx.getSpentTxId());
//                        pAddressTx.setSpentHeight(addressTx.getSpentHeight());
//                        pAddressTx.setSpentIndex(addressTx.getSpentIndex());
//                    }
//                }
//                transactionService.saveTrans(transaction, pAddressTxList);
//            } catch (Exception e) {
//                logger.error("Batch error " + transaction.getTxId() + " " + e.getMessage(), e);
//            }
//        }
//    }

    public Map getInfo() throws Exception{
        return (Map)callQtumService("getinfo", null);
    }

    public Long getMaxBlockHeightFromDB() {
        return transactionDao.findMaxBlockHeight();
    }

    public void getAllBlockHeightFromDB() throws Exception{
        List<BigInteger> blockHeightList = transactionDao.getAllBlockHeightFromDB();

        List<Integer> missingBlockCount = new ArrayList<Integer>();
        int currentCount = 0;
        for (int i = 0; i < blockHeightList.size(); i++){
            int n = Integer.valueOf(blockHeightList.get(i).toString());
            if (n == 0 || n == 1){
                currentCount++;
                continue;
            }

            for(int j = currentCount; j< n ; j++){
                missingBlockCount.add(j);
            }
            currentCount = n + 1;
        }

        for (Integer n: missingBlockCount) {
            String blockHash = getBlockHash(Long.valueOf(n + 1));
            if (blockHash != null) {
                Thread.sleep(8000);
                syncBlockByBlockHash(blockHash);
            }
        }
    }

    public Double getSubsidy() throws Exception{
        List<Object> paramsList = new ArrayList<Object>();
        Double subsidyValue = Double.valueOf((Long) callQtumService("getsubsidy", paramsList)) / 100000000;

        return subsidyValue;
    }

    private Object callQtumService(String method, Object params) throws Exception{
        Map<String, Object> bodyMap = new HashMap<>();
        bodyMap.put("method", method);
        if (params != null) {
            bodyMap.put("params", params);
        }

        Map<String, String> header = new HashMap<>();
        NetworkUtil.addAuth(username, password, header);
        String url = qtumUrl;
        if (method.equals("searchlogs")) {
            url = eventUrl;
        }
        HashMap result = NetworkUtil.callHttpReq(HttpMethod.POST, url, BeanUtils.objectToJson(bodyMap), header, HashMap.class);
        return result.get("result");
    }

    public List<TransactionVO> getTransactionByAddresses(List<String> addressList, String contractAddress, Long startBlock, Long endBlock , Integer offset,
                                                         Integer limit) throws Exception{
//        Map chainInfo = getInfo();
//        Long blocks = Long.valueOf(chainInfo.get("blocks").toString());
        Long blocks = transactionDao.findMaxBlockHeight();
        if (contractAddress != null) {
            Address address = new Address(CurrentNetParam.get(), HexUtils.fromHexString(contractAddress));
            contractAddress = address.toBase58();
        }
        List<Transaction> transactionList = transactionDaoImpl.findByAddress(addressList, contractAddress, startBlock, endBlock, offset, limit);
        List<TransactionVO> transactionVOList = new ArrayList<>();
        for (Transaction transaction: transactionList) {
            List<AddressTx> addressTxList = addressTxDao.findByTxHash(transaction.getTxId());

            TransactionVO transactionVO = new TransactionVO(blocks);
            BeanUtils.copyProperties(transaction, transactionVO);

            transactionVO.convertAddressTx(addressTxList);

            List<String> uniqueList = new ArrayList<String>();
            for (AddressTx addressTx: addressTxList) {
                if (!addressTx.getIn())
                    continue;

                if (addressTx.getAddress() != null && !uniqueList.contains(addressTx.getSpentTxId() + "_" + addressTx.getIndex())){
                    transactionVO.getInList().add(addressTx);
                    uniqueList.add(addressTx.getSpentTxId() + "_" + addressTx.getIndex());
                    continue;
                }

                if (addressTx.getSpentTxId() != null){
                    List<AddressTx> spendTxList = addressTxDao.findByTxHash(addressTx.getSpentTxId());

                    for (AddressTx spendTx: spendTxList) {
                        if (!spendTx.getIn() && addressTx.getIndex() == spendTx.getIndex() && !uniqueList.contains(addressTx.getSpentTxId() + "_" + addressTx.getIndex())){
                            AddressTx addressTxIn = new AddressTx();
                            BeanUtils.copyProperties(addressTx, addressTxIn);
                            addressTxIn.setAddress(spendTx.getAddress());
                            addressTxIn.setAmount(spendTx.getAmount());
                            transactionVO.getInList().add(addressTxIn);
                            uniqueList.add(addressTx.getSpentTxId() + "_" + addressTx.getIndex());
                        }
                    }
                }
            }

            transactionVOList.add(transactionVO);
        }
        return transactionVOList;
    }

    public boolean isInteger(String str) {
        Pattern pattern = Pattern.compile("^[-\\+]?[\\d]*$");
        return pattern.matcher(str).matches();
    }

    public String getLatestBlockInfos(Integer limit, Integer offset) throws Exception{

        List<Map> blockInfos = blockInfoDaoImpl.findByLatestBlockInfos(limit, offset);

        return gson.toJson(blockInfos);
    }

    public String getBlockInfo(String blockHashOrBlockCount) throws Exception{
        Map blockMap = null;
        if(isInteger(blockHashOrBlockCount)){
            blockMap = blockInfoDaoImpl.findByBlockHeight(Integer.valueOf(blockHashOrBlockCount));
        }
        else{
            blockMap = blockInfoDaoImpl.findByBlockHash(blockHashOrBlockCount);
        }

        return gson.toJson(blockMap);
    }

    public String getAddressInfo(String address, Integer limit, Integer offset) throws Exception{

        return addressInfoDaoImpl.getAddressInfo(address, limit, offset);
    }

    public String getTransactionInfo(String txHash) throws Exception{

        Map txMap = txInfoDaoImpl.getTxInfo(txHash);

        return gson.toJson(txMap);
    }

    public Map<String, TokenInfo> getTokenIndoMap(){
        return tokenInfoDaoImpl.getTokenInfoMap();
    }

    public List<Integer> getHeightList(){
        return blockInfoDao.getAllBlockHeightFromDB();
    }

    public String getTokenBalance(String address){
        Map tokens = new HashMap();

        try{
            Map<String, TokenInfo>  tokenInfoMap = tokenInfoDaoImpl.getTokenInfoMap();

            String param = QtumUtils.base58ToSha160(address);
            if (param.length() < 64) {
                String patchStr = "";
                for(int i = param.length(); i < 64; i++){
                    patchStr += "0";
                }

                param = patchStr + param;
            }

            List txidParamList = null;
            for (Map.Entry entry: tokenInfoMap.entrySet()) {
                txidParamList = new ArrayList();
                txidParamList.add(((TokenInfo)entry.getValue()).getContractAddress());
                txidParamList.add("70a08231" + param);

                HashMap map = (HashMap)callQtumService("callcontract", txidParamList);
                if(map.get("executionResult") != null){
                    BigDecimal value = new BigDecimal(Long.parseLong((String) ((Map)map.get("executionResult")).get("output"), 16));
                    if (value.compareTo(new BigDecimal(0)) <= 0){
                        continue;
                    }

                    tokens.put(((TokenInfo)entry.getValue()).getSymbol(), value.divide(new BigDecimal(Math.pow(10, ((TokenInfo)entry.getValue()).getDecimal()))));
                }
            }

        }
        catch (Exception e){
            e.printStackTrace();
        }


        return gson.toJson(tokens);
    }

    public String queryByParam(String param){
        Map map = new HashMap();
        if (isInteger(param)){
            map = blockInfoDaoImpl.findByBlockHeight(Integer.valueOf(param));
            return gson.toJson(map);
        }

        int paramLength = param.length();
        if(paramLength == 64){
            //参数长度为64为区块hash或者交易Hash
            map = blockInfoDaoImpl.findByBlockHash(param);
            if(map == null || map.size() == 0){
                map = txInfoDaoImpl.getTxInfo(param);
            }
        }
        else if(paramLength == 40 || paramLength == 34){
            //参数长度为40为合约地址---参数长度为35为账户地址
            return addressInfoDaoImpl.getAddressInfo(param,10,0);
        }

        return gson.toJson(map);
    }

    public String getBlockAndTx(){
        Map resultMap = new HashMap();

        List<Map>  blockInfos = blockInfoDaoImpl.findByLatestBlockInfos(10, 0);
        List<Map> txInfos = txInfoDaoImpl.findByLatestTxInfos(20, 0);

        resultMap.put("blockHeight", blockInfoDao.findMaxBlockHeight());
        resultMap.put("blocks", blockInfos);
        resultMap.put("txs", txInfos);

        return gson.toJson(resultMap);
    }
}
