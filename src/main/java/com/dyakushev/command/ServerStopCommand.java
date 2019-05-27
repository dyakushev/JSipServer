package com.dyakushev.command;

import com.dyakushev.JSipServer;

public class ServerStopCommand extends Command {
    @Override
    public boolean execute() {
        this.jSipServer.stopServer();
        return true;
    }

    public ServerStopCommand(JSipServer jSipServer) {
        super(jSipServer);
    }
}
