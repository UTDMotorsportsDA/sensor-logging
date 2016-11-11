package fsae.da.car;

import java.net.UnknownHostException;
import java.time.Duration;
import java.util.Scanner;

public class CarMain {
    public static void main(String[] args) {
        // communication parameters
        final String SERVER_IP = args[0];
        final int SERVER_PORT = Integer.parseInt(args[1]);
        Scanner stdin = new Scanner(System.in);
        String input;

        // example sensors
        Duration[] accelerationDurations = { Duration.ofMillis(10), Duration.ofMillis(50), Duration.ofMillis(50) };
        Sensor[] sensors = { new LSM303AccelerationSensor("acceleration", accelerationDurations, 2) };
//        Sensor[] sensors = ConfigLoader.getSensorsFromFile(args[2]);

        // client to collect and transmit data, server to receive data
        DataLoggerClient client = null;
        try {
            client = new DataLoggerClient(SERVER_IP, SERVER_PORT, sensors);
        } catch (UnknownHostException e) {
            e.printStackTrace();
            return;
        }

        // run logger on a thread to allow additional tasks
        new Thread(client).start();

        // wait for user to quit
        while(Character.toUpperCase(stdin.next().charAt(0)) != 'q');

        // quit
        client.end();
    }
}
