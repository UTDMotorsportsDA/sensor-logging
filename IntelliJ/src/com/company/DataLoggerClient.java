package com.company;

import java.io.IOException;
import java.io.PrintWriter;
import java.net.Socket;
import java.time.Duration;
import java.time.Instant;
import java.util.Arrays;
import java.util.PriorityQueue;
import java.util.Queue;
import java.util.concurrent.PriorityBlockingQueue;

/**
 * Created by brian on 10/16/16.
 */
public class DataLoggerClient implements Runnable {

    private String server = null;
    private int port = 0;
    private Queue<Sensor> sensorQueue = new PriorityBlockingQueue<Sensor>();
    boolean done = false;

    public DataLoggerClient(String serverHostname, int serverPort, Sensor[] sensors) {
        server = serverHostname;
        port = serverPort;
        for(Sensor s : sensors) {
            sensorQueue.add(s);
        }
    }

    // intended to inform this instance of a new update interval immediately
    public void reAddSensor(Sensor s) {
        // if sensor is in queue, remove
        sensorQueue.remove(s);

        // add sensor to queue (if sensor was in queue, moves it to the new update period)
        sensorQueue.add(s);
    }

    @Override
    public void run() {
        // open a socket and writer to send data to the server
        try(Socket outgoingSocket = new Socket(server, port);
            PrintWriter outgoingWriter = new PrintWriter(outgoingSocket.getOutputStream(), true);) {

            System.out.println("Client is up.");

            while(!done) {
                // retrieve a sensor from which to read out of the queue
                Sensor currentSensor = sensorQueue.poll();

                // wait until time to update
                Duration negativeDelta = Duration.between(currentSensor.timeOfNextUpdate(), Instant.now());
                if(negativeDelta.isNegative()) {
                    System.out.println("    wait " + negativeDelta.negated().toNanos() + " nanos for sensor " + currentSensor.getLabel());
                    try {
                        Thread.sleep(-1 * negativeDelta.toMillis(), (int)(-1 * (negativeDelta.toNanos() - 1000 * negativeDelta.toMillis())));
                    } catch (InterruptedException e) {
                        e.printStackTrace();
                    }
                }
                else
                    System.out.println("    wait " + 0 + " nanos for sensor " + currentSensor.getLabel());

                // get updated value
                outgoingWriter.println(currentSensor.getDataPoint());

                // re-enqueue sensor for next update
                sensorQueue.add(currentSensor);
            }
        } catch (IOException e) {
            e.printStackTrace();
        }
    }

    // allow client to finish
    public void end() { done = true; }
}
