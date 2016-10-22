package com.company;

import java.time.Duration;
import java.util.Scanner;

/**
 * Created by brian on 10/17/16.
 */
public class ClientMain {
    public static void main(String[] args) {
        // communication parameters
        final String SERVER_IP = args[0];
        final int SERVER_PORT = Integer.parseInt(args[1]);

        // example sensors
        Sensor[] sensors = {
                new SpoofSensor("2", new Duration[] {Duration.ofMillis(10), Duration.ofSeconds(2), Duration.ofMillis(100)}, 200.f),
                new SpoofSensor("3", new Duration[] {Duration.ofMillis(10), Duration.ofSeconds(3), Duration.ofMillis(300)}, 100.f),
                new SpoofSensor("5", new Duration[] {Duration.ofMillis(10), Duration.ofSeconds(5), Duration.ofMillis(500)}, 800.f)
        };

        // client to collect and transmit data, server to receive data
        DataLoggerClient client = new DataLoggerClient(SERVER_IP, SERVER_PORT, sensors);

        // run logger on a thread to allow additional tasks
        new Thread(client).start();

        // wait for some user input before ending
        new Scanner(System.in).next();

        // quit
        client.end();
    }
}
