package com.aethercoder.vo;

import com.aethercoder.util.CurrentNetParam;
import com.subgraph.orchid.encoders.Hex;
import org.bitcoinj.core.Address;
import org.springframework.util.StringUtils;

import java.math.BigInteger;

/**
 * Created by hepengfei on 22/01/2018.
 */
public class TokenTransfer {
    final static String TRANSFER = "a9059cbb";
    private String raw;
    private String toAddress;
    private BigInteger value;

    public TokenTransfer(String raw) {
        super();
        this.raw = raw;
        this.transferRaw();
    }

    public void transferRaw() {
        if (!raw.startsWith(TokenTransfer.TRANSFER)) {
            throw new RuntimeException("ASM is not transfer: " + raw);
        }
        String subRaw = raw.replace(TRANSFER, "");
        String addressHash160 = subRaw.substring(0, 64);
        addressHash160 = addressHash160.substring(24);
        String strAmountHex = subRaw.substring(64);

        Address address = new Address(CurrentNetParam.get(), Hex.decode(addressHash160));
        toAddress = address.toString();

        value = new BigInteger(strAmountHex, 16);
    }


    public String getToAddress() {
        return toAddress;
    }

    public BigInteger getValue() {
        return value;
    }
}
