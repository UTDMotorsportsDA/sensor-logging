package fsae.da.car;

import fsae.da.DataPoint;

import java.io.BufferedOutputStream;
import java.io.IOException;
import java.io.OutputStream;
import java.net.DatagramSocket;
import java.net.Socket;
import java.net.UnknownHostException;
import java.util.ArrayList;
import java.util.Queue;
import java.util.Scanner;
import java.util.concurrent.BlockingQueue;
import java.util.concurrent.PriorityBlockingQueue;

public class CarMain {
    public static void main(String[] args) {
        // communication parameters
        final String BROADCAST_IP = args[0], PIT_IP = args[2];
        final int BROADCAST_PORT = Integer.parseInt(args[1]), PIT_PORT = Integer.parseInt(args[3]);
        Scanner stdin = new Scanner(System.in);

        // load sensors
        Sensor[] sensors = ConfigLoader.getSensorsFromFile(args[4]);

        // client to collect and enqueue data, transmitter to send/broadcast
        DataLoggerClient client = null;
        DataTransmitter tx = null;
        BlockingQueue<DataPoint> dataQueue = new PriorityBlockingQueue<>(); // get the data out in timestamp order
        Socket clientSocket = null;
        ArrayList<OutputStream> streams = new ArrayList<>();

        try {
            clientSocket = new Socket(PIT_IP, PIT_PORT);

            streams.add(new BufferedOutputStream(clientSocket.getOutputStream()));
            client = new DataLoggerClient(sensors, dataQueue);
            tx = new DataTransmitter(dataQueue, streams.toArray(new OutputStream[0]), BROADCAST_IP, BROADCAST_PORT);
        } catch (UnknownHostException e) {
            e.printStackTrace();
            return;
        } catch (IOException e) {
            e.printStackTrace();
        }

        // run logger on a thread to allow additional tasks
        Thread clientThread = new Thread(client);
        clientThread.start();

        // give TX a thread to preserve data integrity
        Thread txThread = new Thread(tx);
        txThread.start();

        // wait for user to quit
        while(Character.toUpperCase(stdin.next().charAt(0)) != 'q');

        // quit
        client.end();
        tx.end();
        try {
            clientThread.join();
            txThread.join();
        } catch (InterruptedException e) {
            e.printStackTrace();
        }
    }
}
