package com.company;

import java.util.Scanner;

/**
 * Created by brian on 10/17/16.
 */
public class ClientMain {
    public static void main(String[] args) {
        // communication parameters
        final String SERVER_IP = args[0];
        final int SERVER_PORT = Integer.parseInt(args[1]);
        Scanner stdin = new Scanner(System.in);
        String input;

        // example sensors
        Sensor[] sensors = ConfigLoader.getSensorsFromFile(args[2]);

        // client to collect and transmit data, server to receive data
        DataLoggerClient client = new DataLoggerClient(SERVER_IP, SERVER_PORT, sensors);

        // run logger on a thread to allow additional tasks
        new Thread(client).start();

        // handle keyboard input and quit if needed
        while(!(input = stdin.next()).equals("quit"))
            if(input.charAt(0) == 'o')
                InputHandler.ohShit(sensors);
            else if(input.charAt(0) == 'z')
                InputHandler.zzz(sensors);
            else
                InputHandler.toggleCriticalState(input.charAt(0), sensors);

        // quit
        client.end();
    }
}
