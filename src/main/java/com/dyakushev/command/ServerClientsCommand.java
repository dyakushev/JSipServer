package com.dyakushev.command;


import com.dyakushev.JSipServer;

public class ServerClientsCommand extends Command {
    @Override
    public boolean execute() {
        System.out.println(jSipServer.getRegisteredClientService().findAllClients());
        return true;
    }

    public ServerClientsCommand(JSipServer jSipServer) {
        super(jSipServer);

    }
}
