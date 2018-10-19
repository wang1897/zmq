package com.aethercoder.util;


import org.apache.tomcat.util.buf.HexUtils;
import org.bitcoinj.core.Address;

/**
 * Created by hepengfei on 08/02/2018.
 */
public class QtumUtils {
    public static String sha160ToBase58(String sha160) {
        Address address = new Address(CurrentNetParam.get(), HexUtils.fromHexString(sha160));
        return address.toBase58();
    }

    public static String base58ToSha160(String base58) {
        Address address = Address.fromBase58(CurrentNetParam.get(), base58);
        return HexUtils.toHexString(address.getHash160());
    }

    public static void main(String... args) {
        System.out.println(QtumUtils.sha160ToBase58("6b8bf98ff497c064e8f0bde13e0c4f5ed5bf8ce7"));
        System.out.println(QtumUtils.base58ToSha160("QUUnV4thoGncXQpK32j82Hg9qdurij4bXo"));
    }
}
