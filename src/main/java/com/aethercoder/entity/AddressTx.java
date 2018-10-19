package com.aethercoder.entity;

import com.aethercoder.util.CurrentNetParam;
import com.fasterxml.jackson.annotation.JsonIgnore;
import com.fasterxml.jackson.annotation.JsonProperty;
import org.apache.tomcat.util.buf.HexUtils;
import org.bitcoinj.core.Address;
import org.hibernate.annotations.Type;

import javax.persistence.*;
import java.math.BigDecimal;

/**
 * Created by hepengfei on 20/01/2018.
 */
@Entity
@Table(name = "address_tx")
public class AddressTx extends BaseEntity {
    @Column(name = "address")
    private String address;

    @Column(name = "tx_hash")
    @JsonProperty("txid")
    private String txHash;

    @Column(name = "amount")
    @JsonProperty("value")
    private BigDecimal amount;

    @Column(name = "in_out")
    private Boolean isIn;

    @Column(name = "is_token")
    private Boolean isToken = false;

    @Column(name = "token_addr")
    @JsonProperty("contractAddress")
    private String tokenAddr;

    @Transient
    @JsonProperty("contractAddressSha160")
    private String tokenAddrSha160;

    @Column(name = "token_amount")
    @JsonProperty("tokenValue")
    private String tokenAmount;

    @Column(name = "spent_tx_id")
    private String spentTxId;

    @Column(name = "spent_index")
    private Integer spentIndex;

    @Column(name = "spent_height")
    private Long spentHeight;

    @Column(name = "tx_index")
    @JsonProperty("n")
    private Integer index;

    @Column(name = "asm")
    @Type(type="text")
    private String asm;

    @Column(name = "hex")
    @Type(type="text")
    private String hex;

    @Column(name = "token_from_addr")
    private String tokenFromAddr;

    @Column(name = "token_logged")
    private Boolean tokenLogged = false;

    public String getAddress() {
        return address;
    }

    public void setAddress(String address) {
        this.address = address;
    }

    public String getTxHash() {
        return txHash;
    }

    public void setTxHash(String txHash) {
        this.txHash = txHash;
    }

    public BigDecimal getAmount() {
        return amount;
    }

    public void setAmount(BigDecimal amount) {
        this.amount = amount;
    }

    @JsonIgnore
    public Boolean getIn() {
        return isIn;
    }

    public void setIn(Boolean in) {
        isIn = in;
    }

    @JsonIgnore
    public Boolean getToken() {
        return isToken;
    }

    public void setToken(Boolean token) {
        isToken = token;
    }

    public String getTokenAddr() {
        return tokenAddr;
    }

    public void setTokenAddr(String tokenAddr) {
        this.tokenAddr = tokenAddr;
    }

    public String getTokenAmount() {
        return tokenAmount;
    }

    public void setTokenAmount(String tokenAmount) {
        this.tokenAmount = tokenAmount;
    }

    public String getSpentTxId() {
        return spentTxId;
    }

    public void setSpentTxId(String spentTxId) {
        this.spentTxId = spentTxId;
    }

    public Integer getIndex() {
        return index;
    }

    public void setIndex(Integer index) {
        this.index = index;
    }

    public String getAsm() {
        return asm;
    }

    public void setAsm(String asm) {
        this.asm = asm;
    }

    public String getHex() {
        return hex;
    }

    public void setHex(String hex) {
        this.hex = hex;
    }

    public Integer getSpentIndex() {
        return spentIndex;
    }

    public void setSpentIndex(Integer spentIndex) {
        this.spentIndex = spentIndex;
    }

    public Long getSpentHeight() {
        return spentHeight;
    }

    public void setSpentHeight(Long spentHeight) {
        this.spentHeight = spentHeight;
    }

    public String getTokenFromAddr() {
        return tokenFromAddr;
    }

    public void setTokenFromAddr(String tokenFromAddr) {
        this.tokenFromAddr = tokenFromAddr;
    }

    public String getTokenAddrSha160() {
        if (tokenAddr != null) {
            Address address = Address.fromBase58(CurrentNetParam.get(), tokenAddr);
            tokenAddrSha160 = HexUtils.toHexString(address.getHash160());
        }
        return tokenAddrSha160;
    }

    public Boolean getTokenLogged() {
        return tokenLogged;
    }

    public void setTokenLogged(Boolean tokenLogged) {
        this.tokenLogged = tokenLogged;
    }

    @Override
    public boolean equals(Object obj) {
        if (!(obj instanceof AddressTx)) {
            return false;
        }
        AddressTx addressTx = (AddressTx)obj;
        if (addressTx.getTxHash().equals(txHash) && addressTx.getIn().equals(isIn) && addressTx.getIndex().equals(index)) {
            return true;
        }
        return false;
    }
}
