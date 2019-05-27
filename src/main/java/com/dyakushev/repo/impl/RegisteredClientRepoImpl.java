package com.dyakushev.repo.impl;

import com.dyakushev.model.ents.RegisteredClient;
import com.dyakushev.repo.RegisteredClientRepo;
import com.dyakushev.utils.RegisteredClientBuilder;

import javax.sip.message.Request;
import java.util.ArrayList;
import java.util.List;

public class RegisteredClientRepoImpl implements RegisteredClientRepo {
    private List<RegisteredClient> clientList;

    public RegisteredClientRepoImpl() {
        this.clientList = new ArrayList<>();
    }

    @Override
    public void addClient(Request request) {
        RegisteredClient registeredClient = RegisteredClientBuilder.build(request);
        if (!clientList.contains(registeredClient))
            clientList.add(registeredClient);
    }

    @Override
    public void removeClient(Request request) {
        RegisteredClient registeredClient = RegisteredClientBuilder.build(request);
        if (clientList.contains(registeredClient))
            clientList.remove(registeredClient);
    }

    @Override
    public void removeClientById(Long id) {
    }

    @Override
    public boolean isRegistered(Request request) {
        RegisteredClient registeredClient = RegisteredClientBuilder.build(request);
        return clientList.contains(registeredClient);
    }

    @Override
    public List<RegisteredClient> findAllClients() {
        return clientList;
    }

    @Override
    public RegisteredClient getClient(Request request) {
        RegisteredClient registeredClient = RegisteredClientBuilder.build(request);
        for (RegisteredClient reg : clientList) {
            if (reg.equals(registeredClient)) {
                return reg;
            }
        }
        return null;
    }
}
