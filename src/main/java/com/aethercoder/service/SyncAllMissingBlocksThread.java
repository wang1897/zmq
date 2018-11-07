package com.aethercoder.service;

public class SyncAllMissingBlocksThread implements Runnable {

    private QtumService qtumService;

    public SyncAllMissingBlocksThread(QtumService qtumService){
        this.qtumService = qtumService;
    }

    /**
     * 执行操作
     */
    @Override
    public void run(){
        try{
//            qtumService.getAllBlockHeightFromDB();
        }
        catch (Exception e){
            e.printStackTrace();
        }
    }
}
