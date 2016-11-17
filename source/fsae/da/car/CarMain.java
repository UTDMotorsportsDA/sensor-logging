package fsae.da.car;

import fsae.da.DataPoint;

import java.io.BufferedOutputStream;
import java.io.IOException;
import java.io.OutputStream;
import java.net.InetSocketAddress;
import java.net.Socket;
import java.net.SocketAddress;
import java.net.UnknownHostException;
import java.util.ArrayList;
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

        // client to collect and enqueue data, transmitter to send/broadcast from the queue
        DataLogger logger = null;
        DataTransmitter tx = null;
        BlockingQueue<DataPoint> dataQueue = new PriorityBlockingQueue<>(); // get the data out in timestamp order

        // destinations for the transmitter
        ArrayList<OutputStream> streams = new ArrayList<>();

        // try first time to make a socket
        Socket clientSocket = null;
        SocketAddress clientSockAddr = null;
        try {
            clientSocket = new Socket(PIT_IP, PIT_PORT);
        } catch (IOException | SecurityException e) {
            clientSocket = null;
        }

        try {
            // open a TCP socket to the pit for reliability, get its output stream (only if connection succeeded)
            if(clientSocket != null) streams.add(new BufferedOutputStream(clientSocket.getOutputStream()));

            // transmitter will send data points from the queue
            // let UnknownHostException propagate back here
            tx = new DataTransmitter(dataQueue, streams, BROADCAST_IP, BROADCAST_PORT);
        } catch (UnknownHostException e) {
            e.printStackTrace();
            return;
        } catch (IOException e) {
            e.printStackTrace();
        }

        // create the logger to enqueue data points from sensors
        logger = new DataLogger(sensors, dataQueue);

        // run logger on a thread to allow additional tasks
        Thread loggerThread = new Thread(logger);
        loggerThread.start();

        // give TX a thread to preserve data integrity
        Thread txThread = new Thread(tx);
        txThread.start();

        // wait for user to quit
        while(Character.toUpperCase(stdin.next().charAt(0)) != 'q') {
            if(clientSocket == null) { // keep trying to connect to the pit w/ 1-second timeouts
                try {
                    clientSocket = new Socket();
                    clientSocket.connect(new InetSocketAddress(PIT_IP, PIT_PORT), 1000);
                } catch (IOException | SecurityException e) {
                    clientSocket = null;
                    continue;
                }

                // we're here because connection succeeded, so add to stream list
                try {
                    streams.add(new BufferedOutputStream(clientSocket.getOutputStream()));
                } catch (IOException e) {
                    e.printStackTrace(); // most likely won't happen
                }
            }
        }

        // quit
        logger.end();
        tx.end();
        try {
            loggerThread.join();
            txThread.join();
        } catch (InterruptedException e) {
            e.printStackTrace();
        }
    }
}
