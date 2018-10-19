package com.aethercoder.entity;

import org.hibernate.annotations.Type;

import javax.persistence.*;
import java.util.Date;

/**
 * Created by hepengfei on 20/01/2018.
 */
@Entity
@Table(name = "transaction")
public class Transaction extends BaseEntity {

    @Column(name = "tx_id")
    private String txId;

    @Column(name = "tx_raw")
    @Type(type="text")
    private String txRaw;

    @Column(name = "time")
    private Date time;

    @Column(name = "block_hash")
    private String blockHash;

    @Column(name = "block_height")
    private Long blockHeight;

    @Column(name = "size")
    private Integer size;

    @Column(name = "locktime")
    private Integer locktime;

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
}
