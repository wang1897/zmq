package com.aethercoder;

import com.aethercoder.service.*;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.boot.CommandLineRunner;
import org.springframework.boot.SpringApplication;
import org.springframework.boot.autoconfigure.SpringBootApplication;
import org.springframework.scheduling.annotation.EnableScheduling;

import java.io.IOException;
import java.util.concurrent.BlockingDeque;
import java.util.concurrent.LinkedBlockingDeque;

/**
 * Created by hepengfei on 18/01/2018.
 */

@SpringBootApplication
@EnableScheduling
public class Request implements CommandLineRunner {

    private Logger logger = LoggerFactory.getLogger(Request.class);

    public static Integer PID;

    @Autowired
    private ZMQSubService zmqService;

    @Autowired
    private QueueSubService queueSubService;

    @Autowired
    private CmdService cmdService;


    final private BlockingDeque<String> blockingDeque = new LinkedBlockingDeque<>();

    public static void main(String args[]) throws Exception{
        SpringApplication.run(Request.class, args);
    }

    @Override
    public void run(String[] args) throws Exception {
        zmqService.setBlockingDeque(blockingDeque);
        queueSubService.setBlockingDeque(blockingDeque);
        Thread producer = new Thread(zmqService);
        Thread consumer = new Thread(queueSubService);
        producer.start();
        consumer.start();

    }

}
