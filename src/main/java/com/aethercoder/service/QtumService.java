package com.aethercoder.service;

import com.aethercoder.dao.BlockInfoDao;
import com.aethercoder.dao.impl.AddressInfoDaoImpl;
import com.aethercoder.dao.impl.BlockInfoDaoImpl;
import com.aethercoder.dao.impl.TokenInfoDaoImpl;
import com.aethercoder.dao.impl.TxInfoDaoImpl;
import com.aethercoder.entity.AddressInfo;
import com.aethercoder.entity.BlockInfo;
import com.aethercoder.entity.TokenInfo;
import com.aethercoder.entity.TxInfo;
import com.aethercoder.util.BeanUtils;
import com.aethercoder.util.NetworkUtil;
import com.aethercoder.util.QtumUtils;
import com.aethercoder.vo.TokenTransfer;
import com.google.gson.Gson;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.beans.factory.annotation.Value;
import org.springframework.http.HttpMethod;
import org.springframework.stereotype.Service;

import java.math.BigDecimal;
import java.util.*;
import java.util.regex.Pattern;

import static com.aethercoder.constants.Constants.QBE_SYMBOL;

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
    private BlockInfoDaoImpl blockInfoDaoImpl;

    @Autowired
    private BlockInfoDao blockInfoDao;

    @Autowired
    private TxInfoDaoImpl txInfoDaoImpl;

    @Autowired
    private AddressInfoDaoImpl addressInfoDaoImpl;

    @Autowired
    private TokenInfoDaoImpl tokenInfoDaoImpl;

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
                                         Map<String, BigDecimal> txFeeMap) throws Exception{
        Map transDetail = getTransDetail(txid);
        List<Map> vin = (List)transDetail.get("vin");

        //地址余额变动的集合
        Map<String, AddressInfo> addressValueMap = new HashMap<String, AddressInfo>();
        List<Map> txVin = new ArrayList<>();
        List<Map> txVout = new ArrayList<>();
        boolean isBaseFlag = false;
        BigDecimal txFee = new BigDecimal(0);

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

                    txFee = txFee.add(new BigDecimal(vinTxMapPer.get("value").toString()));
                    if (addressValueMap.containsKey(address)){
                        AddressInfo addressInfo = addressValueMap.get(address);
                        addressInfo.setBalance_change(addressInfo.getBalance_change().subtract(new BigDecimal(vinTxMapPer.get("value").toString())));
                    }
                    else{
                        //生成输入地址的信息
                        AddressInfo addressInfo = generateAddressInfo(addressInfoList,
                                address,
                                new BigDecimal(vinTxMapPer.get("value").toString()).negate(),
                                txid,
                                (Integer) transDetail.get("time"),
                                height,
                                QBE_SYMBOL,
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
        txFee = handleVoutMap(txFee, transDetail, height, txVout, isBaseFlag, addressInfoList, txFeeMap, addressValueMap);

        txFee = isBaseFlag ? txFeeMap.get("blockFee") : txFee;

        // 生成transaction信息
        generateTxInfo(transDetail, txInfos, txFee, gson.toJson(txVin), gson.toJson(txVout), vin.size(), ((List)transDetail.get("vout")).size(), height);
    }

    private BigDecimal handleVoutMap(BigDecimal txFee,
                                 Map transDetail,
                                 Integer height,
                                 List<Map> txVout,
                                 boolean isBaseFlag,
                                 List<AddressInfo> addressInfoList,
                                 Map<String, BigDecimal> txFeeMap,
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
                txFeeMap.put("blockFee", new BigDecimal(map.get("value").toString()));
            }
            else if(isBaseFlag && 0 != (Integer)map.get("n")){
                txFeeMap.put(address, new BigDecimal(map.get("value").toString()));
            }

            //如果输出地址不在输入列表里面，则为新的地址生成余额变动信息
            if (addressValueMap.containsKey(address)){
                AddressInfo addressInfo = addressValueMap.get(address);
                addressInfo.setBalance_change(addressInfo.getBalance_change().add(new BigDecimal(map.get("value").toString())));
            }
            else{
                generateAddressInfo(addressInfoList,
                        address,
                        new BigDecimal(map.get("value").toString()),
                        (String) transDetail.get("txid"),
                        (Integer)transDetail.get("time"),
                        height,
                        QBE_SYMBOL,
                        null);
            }

            // 生成交易的VoutMap
            setVinAndVout(address, new BigDecimal(map.get("value").toString()), QBE_SYMBOL, scriptPubKey, txVout);

            // 计算交易的手续费时 扣除 花费返回部分
            BigDecimal addressFee = txFeeMap.get(address) == null ? new BigDecimal(0) : txFeeMap.get(address);
            txFee = txFee.subtract(new BigDecimal(map.get("value").toString()).add(addressFee));
//            txFee -= (double)map.get("value") + (txFeeMap.get(address) == null ? 0 : txFeeMap.get(address));
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


    public Map getInfo() throws Exception{
        return (Map)callQtumService("getinfo", null);
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
