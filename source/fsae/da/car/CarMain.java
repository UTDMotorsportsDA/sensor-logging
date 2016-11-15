package fsae.da.car;

import fsae.da.DataPoint;

import java.net.UnknownHostException;
import java.util.Queue;
import java.util.Scanner;
import java.util.concurrent.PriorityBlockingQueue;

public class CarMain {
    public static void main(String[] args) {
        // communication parameters
        final String SERVER_IP = args[0];
        final int SERVER_PORT = Integer.parseInt(args[1]);
        Scanner stdin = new Scanner(System.in);

        // load sensors
        Sensor[] sensors = ConfigLoader.getSensorsFromFile(args[2]);

        // client to collect and transmit data, server to receive data
        DataLoggerClient client = null;
        Queue<DataPoint> dataQueue = new PriorityBlockingQueue<>(); // get the data out in timestamp order
        try {
            client = new DataLoggerClient(SERVER_IP, SERVER_PORT, sensors, dataQueue);
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
