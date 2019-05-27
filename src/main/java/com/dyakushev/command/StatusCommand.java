package com.dyakushev.command;


import com.dyakushev.JSipServer;

import javax.sip.ListeningPoint;

public class StatusCommand extends Command {
    public StatusCommand(JSipServer jSipServer) {
        super(jSipServer);
    }

    public boolean execute() {
        ListeningPoint listeningPoint[] = jSipServer.getSipProvider().getListeningPoints();
        if (listeningPoint.length > 0)
            for (int i = 0; i < listeningPoint.length; i++) {
                System.out.println("Server is listening: " + listeningPoint[i].getIPAddress() + ":" + listeningPoint[i].getPort());
            }
        else
            System.out.println("no listenning points");
        return true;
    }
}
