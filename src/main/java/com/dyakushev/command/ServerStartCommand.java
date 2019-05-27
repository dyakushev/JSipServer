package com.dyakushev.command;

import com.dyakushev.JSipServer;

public class ServerStartCommand extends Command {
    @Override
    public boolean execute() {
        this.jSipServer.init();
        return true;
    }

    public ServerStartCommand(JSipServer jSipServer) {
        super(jSipServer);
    }
}
