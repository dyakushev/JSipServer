package com.dyakushev.utils;

import com.dyakushev.JSipServer;
import com.dyakushev.model.ents.RegisteredClient;

import javax.sip.header.*;
import javax.sip.message.Message;
import javax.sip.message.Request;
import java.time.LocalDateTime;
import java.util.List;
import java.util.Optional;
import java.util.concurrent.Executors;
import java.util.concurrent.ScheduledExecutorService;
import java.util.concurrent.TimeUnit;

public class JSipServerHelper {
    private JSipServer jSipServer;
    private static JSipServerHelper jSipServerHelper;

    private JSipServerHelper(JSipServer jSipServer) {
        this.jSipServer = jSipServer;


    }

    public static JSipServerHelper getInstance(JSipServer jSipServer) {
        JSipServerHelper localInstance = jSipServerHelper;
        if (localInstance == null) {
            synchronized (JSipServerHelper.class) {
                localInstance = jSipServerHelper;
                if (localInstance == null) {
                    jSipServerHelper = localInstance = new JSipServerHelper(jSipServer);
                }
            }
        }

        return localInstance;
    }

    /*
     * Print messages
     * */

    synchronized public void printMessage(Message message) {

        printMessage(message.toString());
    }

    /*
     * Print messages
     * */
    synchronized public void printMessage(String message) {

        System.out.println(message);
    }


    public static int getExpiredTime(Request request) {
        ExpiresHeader expiresHeader = (ExpiresHeader) request.getHeader(ExpiresHeader.NAME);
        return expiresHeader.getExpires();

    }


}
