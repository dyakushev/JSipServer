package com.dyakushev;

import com.dyakushev.command.*;


import java.io.BufferedReader;
import java.io.IOException;
import java.io.InputStreamReader;

public class Main {
    private static CommandHistory history = new CommandHistory();

    public static void main(String[] args) {
        JSipServer b2BUA = new JSipServer();
        executeCommand(new ServerStartCommand(b2BUA));
      //  b2BUA.init();
       BufferedReader reader =
                new BufferedReader(new InputStreamReader(System.in));
        try {
            while (true) {
                System.out.println("Please enter a command");
                String command = reader.readLine();
                switch (command) {
                    case "status":
                        executeCommand(new StatusCommand(b2BUA));
                        break;
                    case "start":
                        executeCommand(new ServerStartCommand(b2BUA));
                        break;
                    case "stop":
                        executeCommand(new ServerStopCommand(b2BUA));
                        break;
                    case "clients":
                        executeCommand(new ServerClientsCommand(b2BUA));
                        break;

                    case "exit":
                        System.exit(1);
                }
            }
        } catch (IOException e) {
        }


    }

    private static void executeCommand(Command command) {
        if (command.execute()) {
            history.push(command);
        }
    }
}
