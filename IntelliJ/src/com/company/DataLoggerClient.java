package com.company;

import java.io.IOException;
import java.io.PrintWriter;
import java.net.MalformedURLException;
import java.net.Socket;
import java.sql.Ref;
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
    private Queue<ComparableSensor> sensorQueue = new PriorityBlockingQueue<ComparableSensor>();
    boolean done = false;
    // DataLoggerClient is solely intended to report to the pit (either immediately or to a file for later)
    private static final RefreshType DLC_REFRESH_TYPE = RefreshType.PIT;

    public DataLoggerClient(String serverHostname, int serverPort, Sensor[] sensors) {
        server = serverHostname;
        port = serverPort;
        for(Sensor s : sensors) {
            sensorQueue.add(s.asComparable(DLC_REFRESH_TYPE));
        }
    }

    // intended to inform this instance of a new update interval immediately
    public void renewSensor(Sensor s) {
        // if sensor is in queue, remove
        sensorQueue.remove(s.asComparable(DLC_REFRESH_TYPE));

        // add sensor to queue (if sensor was in queue, moves it to the new update period)
        sensorQueue.add(s.asComparable(DLC_REFRESH_TYPE));
    }

    @Override
    public void run() {
        // start the sensor update thread
        SensorUpdater updater0 = new SpoofSensorUpdater(this);

        for(ComparableSensor cs : sensorQueue.toArray(new ComparableSensor[sensorQueue.size()]))
            updater0.addSensor(cs.sensor());

        Thread updater0Thread = new Thread(updater0);
        updater0Thread.start();

        // open a socket and writer to send data to the server
        try(Socket outgoingSocket = new Socket(server, port);
            PrintWriter outgoingWriter = new PrintWriter(outgoingSocket.getOutputStream(), true);) {

            System.out.println("Client is up.");

            while(!done) {
                // retrieve a sensor from which to read out of the queue
                ComparableSensor currentComparableSensor = sensorQueue.poll();
                Sensor currentSensor = currentComparableSensor.sensor();

                // wait until time to update
                Duration negativeDelta = Duration.between(currentComparableSensor.nextRefresh(), Instant.now());
                if(negativeDelta.isNegative()) {

                    long millis = -1 * negativeDelta.toMillis();
                    int nanos = Math.max(0, -1 * (int)negativeDelta.plusMillis(millis).toNanos());

                    System.out.println("    wait " + millis + " millis, " + nanos + " nanos for sensor " + currentSensor.getLabel());
                    try {
                        Thread.sleep(millis, nanos);
                    } catch (InterruptedException e) {
                        e.printStackTrace();
                    }
                }
                else
                    System.out.println("    wait " + 0 + " nanos for sensor " + currentSensor.getLabel());

                // get updated value
                outgoingWriter.println(currentComparableSensor.sensor().getLabel() + "=" + currentSensor.getCurrent());

                // re-enqueue sensor for next update
                sensorQueue.add(currentComparableSensor);
            }
        } catch (IOException e) {
            e.printStackTrace();
        } finally {
            try {
                updater0Thread.join();
            } catch (InterruptedException e) {
                e.printStackTrace();
            }
        }

    }

    // allow client to finish
    public void end() { done = true; }
}
