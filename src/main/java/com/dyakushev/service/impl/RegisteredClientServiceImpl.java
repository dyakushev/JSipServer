package com.dyakushev.service.impl;

import com.dyakushev.model.ents.RegisteredClient;
import com.dyakushev.repo.impl.RegisteredClientRepoImpl;
import com.dyakushev.service.RegisteredClientService;

import javax.sip.message.Request;
import java.util.List;

public class RegisteredClientServiceImpl implements RegisteredClientService {
    private RegisteredClientRepoImpl registeredClientRepo;

    public RegisteredClientServiceImpl() {
        this.registeredClientRepo = new RegisteredClientRepoImpl();
    }

    @Override
    public void addClient(Request request) {
        registeredClientRepo.addClient(request);
        //registeredClientRepo.addClient();

    }

    @Override
    public void removeClient(Request request) {
        registeredClientRepo.removeClient(request);

    }

    @Override
    public boolean isRegistered(Request request) {
        return registeredClientRepo.isRegistered(request);
    }

    @Override
    public List<RegisteredClient> findAllClients() {
        return registeredClientRepo.findAllClients();
    }

    @Override
    public RegisteredClient getClient(Request request) {
        return registeredClientRepo.getClient(request);
    }
}
