package com.aethercoder.vo;

import com.aethercoder.entity.AddressTx;
import com.fasterxml.jackson.annotation.JsonIgnore;
import com.fasterxml.jackson.annotation.JsonInclude;
import com.fasterxml.jackson.annotation.JsonProperty;

import java.util.ArrayList;
import java.util.Date;
import java.util.List;

/**
 * Created by hepengfei on 25/01/2018.
 */
@JsonInclude(JsonInclude.Include.NON_NULL)
public class TransactionVO {
    @JsonProperty("txid")
    private String txId;

    @JsonIgnore
    private String txRaw;

    @JsonProperty("time")
    private Date time;

    @JsonProperty("blockhash")
    private String blockHash;

    @JsonProperty("height")
    private Long blockHeight;

    @JsonProperty("confirmations")
    private Long confirmations;

    @JsonProperty("size")
    private Integer size;

    @JsonProperty("locktime")
    private Integer locktime;

    @JsonIgnore
    private Long maxHeight;

    @JsonProperty("vin")
    private List<AddressTx> inList = new ArrayList<>();

    @JsonProperty("vout")
    private List<AddressTx> outList = new ArrayList<>();

    public TransactionVO(long maxHeight) {
        this.maxHeight = maxHeight;
    }


    public String getTxId() {
        return txId;
    }

    public void setTxId(String txId) {
        this.txId = txId;
    }

    public String getTxRaw() {
        return txRaw;
    }

    public void setTxRaw(String txRaw) {
        this.txRaw = txRaw;
    }

    public Date getTime() {
        return time;
    }

    public void setTime(Date time) {
        this.time = time;
    }

    public String getBlockHash() {
        return blockHash;
    }

    public void setBlockHash(String blockHash) {
        this.blockHash = blockHash;
    }

    public Long getBlockHeight() {
        return blockHeight;
    }

    public void setBlockHeight(Long blockHeight) {
        this.blockHeight = blockHeight;
        if (blockHeight != null) {
            this.confirmations = this.maxHeight - blockHeight + 1;
        }
    }

    public Integer getSize() {
        return size;
    }

    public void setSize(Integer size) {
        this.size = size;
    }

    public Integer getLocktime() {
        return locktime;
    }

    public void setLocktime(Integer locktime) {
        this.locktime = locktime;
    }

    public List<AddressTx> getInList() {
        return inList;
    }

    public void setInList(List<AddressTx> inList) {
        this.inList = inList;
    }

    public List<AddressTx> getOutList() {
        return outList;
    }

    public void setOutList(List<AddressTx> outList) {
        this.outList = outList;
    }

    public void convertAddressTx(List<AddressTx> addressTxList) {
        for (AddressTx addressTx: addressTxList) {
//            if (addressTx.getIn()) {
//                inList.add(addressTx);
//            } else {
//                outList.add(addressTx);
//            }

            if (!addressTx.getIn()) {
                outList.add(addressTx);
            }
        }
    }
}
