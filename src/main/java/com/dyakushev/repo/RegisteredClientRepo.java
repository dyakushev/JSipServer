package com.dyakushev.repo;

import com.dyakushev.model.ents.RegisteredClient;

import javax.sip.message.Request;
import java.util.List;

public interface RegisteredClientRepo {

    void addClient(Request request);

    void removeClient(Request request);

    void removeClientById(Long id);

    boolean isRegistered(Request request);

    List<RegisteredClient> findAllClients();

    RegisteredClient getClient(Request request);

}
