package com.aethercoder.service;

import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import org.springframework.beans.factory.annotation.Value;
import org.springframework.stereotype.Service;

import java.io.IOException;
import java.lang.reflect.Field;

/**
 * Created by hepengfei on 25/01/2018.
 */
@Service
public class CmdService {

    private Logger logger = LoggerFactory.getLogger(CmdService.class);

    @Value("${qtum.qtumd}")
    private String qtumdLoc;

    public Integer startQtum() throws IOException, NoSuchFieldException, IllegalAccessException{
        logger.info("Start qtumd: " + qtumdLoc);
        Process process = Runtime.getRuntime().exec(qtumdLoc);
        if(process.getClass().getName().equals("java.lang.UNIXProcess")) {
            /* get the PID on unix/linux systems */
            Field f = process.getClass().getDeclaredField("pid");
            f.setAccessible(true);
            int pid = f.getInt(process);
            logger.info("qtumd started PID: " + pid);
            return pid;
        }
        return null;
    }

    public void stopQtum(int pid) throws IOException {
        logger.info("kill qtumd PID: " + pid);
        Process process = Runtime.getRuntime().exec("kill -9 " + pid);
    }
}
