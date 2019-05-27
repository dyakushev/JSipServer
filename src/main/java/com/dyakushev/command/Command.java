package com.dyakushev.command;

import com.dyakushev.JSipServer;

public abstract class Command {
    public JSipServer jSipServer;

    public abstract boolean execute();

    public Command(JSipServer jSipServer) {
        this.jSipServer = jSipServer;
    }
}
