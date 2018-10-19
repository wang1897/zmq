package com.aethercoder.util;

import org.bitcoinj.core.NetworkParameters;
import org.bitcoinj.params.QtumMainNetParams;

/**
 * Created by hepengfei on 25/01/2018.
 */
public class CurrentNetParam {
    public static NetworkParameters get(){
        return QtumMainNetParams.get();
    }
}
