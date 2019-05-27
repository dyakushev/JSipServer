package com.dyakushev.utils;

import com.dyakushev.model.ents.RegisteredClient;

import javax.sip.header.*;
import javax.sip.message.Request;

public class RegisteredClientBuilder {

    public static RegisteredClient build(Request request) {
        RegisteredClient registeredClient = new RegisteredClient();
        //Call-ID
        String callId = ((CallIdHeader) request.getHeader("Call-ID")).getCallId();
        registeredClient.setCallId(callId);
        //Contact header
        ContactHeader contactHeader = (ContactHeader) request.getHeader("Contact");
        registeredClient.setContactHeader(contactHeader);
        //Expire time
        int expireTime = ((ExpiresHeader) request.getHeader("Expires")).getExpires();
        registeredClient.setExpireTime(expireTime);
        //From header
        FromHeader fromHeader = (FromHeader) request.getHeader("From");
        registeredClient.setFromHeader(fromHeader);
        //To header
        ToHeader toHeader = (ToHeader) request.getHeader("To");
        registeredClient.setToHeader(toHeader);

        return registeredClient;
    }
}
