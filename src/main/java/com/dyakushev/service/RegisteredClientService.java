package com.dyakushev.service;

import com.dyakushev.model.ents.RegisteredClient;

import javax.sip.message.Request;
import java.util.List;

public interface RegisteredClientService {

    void addClient(Request request);

    void removeClient(Request request);

    boolean isRegistered(Request request);

    List<RegisteredClient> findAllClients();

    RegisteredClient getClient(Request request);


}
