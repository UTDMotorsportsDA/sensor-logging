package com.company;

import java.util.Scanner;

/**
 * Created by brian on 10/17/16.
 */
public class ServerMain {
    public static void main(String[] args) {
        // server needs only a listening port
        final int SERVER_PORT = Integer.parseInt(args[0]);

        // server object (currently just dumps to stdout)
        DataLoggerServer server = new DataLoggerServer(SERVER_PORT);

        // start on separate thread to allow additional work
        new Thread(server).start();

        // wait for some user input before ending
        new Scanner(System.in).next();

        // quit
        server.end();
    }
}
