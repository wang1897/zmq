package com.aethercoder.service;

import com.aethercoder.Request;
import com.subgraph.orchid.encoders.Hex;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.beans.factory.annotation.Value;
import org.springframework.stereotype.Service;
import org.zeromq.ZMQ;

import javax.xml.bind.DatatypeConverter;
import java.io.IOException;
import java.util.concurrent.BlockingDeque;

/**
 * Created by hepengfei on 23/01/2018.
 */
@Service
public class ZMQSubService implements Runnable{

    private Logger logger = LoggerFactory.getLogger(ZMQSubService.class);

    @Autowired
    private TransactionService transactionService;

    @Autowired
    private QtumService qtumService;

    @Autowired
    private CmdService cmdService;

    @Value("${qtum.zmq}")
    private String zmqUrl;

    private BlockingDeque<String> blockingDeque;

    public BlockingDeque<String> getBlockingDeque() {
        return blockingDeque;
    }

    public void setBlockingDeque(BlockingDeque<String> blockingDeque) {
        this.blockingDeque = blockingDeque;
    }

    private long count = 0L;

    @Override
    public void run() {
        ZMQ.Context context = ZMQ.context(1);
        ZMQ.Socket socket = context.socket(ZMQ.SUB);
        socket.connect(zmqUrl);
        socket.subscribe("");
        while (!Thread.currentThread().isInterrupted()) {
            String blockHash = "";
            try {
                logger.info("waiting for receive");
                socket.recv();
                blockHash = new String(Hex.encode(socket.recv(2)));
//                blockHash = "39f11ed4bc12367391e22bbeaaa6fa22799478d165894dcdff251d37008e855f";

                new String(Hex.encode(socket.recv(1)));
                if (blockHash.length() == 64) {
                    logger.info("receive : " + blockHash);
                    blockingDeque.put(blockHash);
                    logger.info("queue size: {}, total: {}", blockingDeque.size(), ++count);
                }
//                break;
            } catch (InterruptedException ie) {
                try {
                    cmdService.stopQtum(Request.PID);
                } catch (IOException e) {
                    logger.error("error close qtumd " + Request.PID, e);
                }
            } catch (Exception e) {
                e.printStackTrace();
                logger.warn("error: " + blockHash + e.getMessage());
            }

            logger.info("receive end");
        }
        try {
            cmdService.stopQtum(Request.PID);
        } catch (IOException e) {
            logger.error("error close qtumd " + Request.PID, e);
        }
        socket.close();
        context.term();
    }
}
