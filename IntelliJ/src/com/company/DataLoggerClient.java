package com.company;

import java.io.IOException;
import java.io.PrintWriter;
import java.net.Socket;
import java.time.Duration;
import java.time.Instant;
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
    private static final RefreshType DLC_REFRESH_TYPE = RefreshType.PIT;
    private Thread clientThread = null;

    public DataLoggerClient(String serverHostname, int serverPort, Sensor[] sensors) {
        server = serverHostname;
        port = serverPort;

        // wrap sensors in objects that implement
        // Comparable for the priority queue
        for(Sensor s : sensors) {
            sensorQueue.add(s.asComparable(DLC_REFRESH_TYPE));
        }
    }

    // intended to inform this instance of a new update interval immediately
    // removes sensor if possible and adds; different from requeueComparableSensor
    public synchronized void renewSensor(Sensor s) {
        // if sensor is in queue, remove
        if(!sensorQueue.remove(s.asComparable(DLC_REFRESH_TYPE)))
            System.out.println("unable to remove sensor, sensorQueue.size() = " + sensorQueue.size());

        // add sensor to queue (if sensor was in queue, moves it to the new update period)
        sensorQueue.add(s.asComparable(DLC_REFRESH_TYPE));

        // kick the logger out of its current wait period to poll for next sensor
        // if a sensor has gone critical, it needs to be handled ASAP
        clientThread.interrupt();
    }

    // part of run()'s queue'd cycle put into a method for thread synchronization
    private synchronized void requeueComparableSensor(ComparableSensor cs) {
        // only re-add sensor if it wasn't added by a call to renewSensor
        if(!sensorQueue.contains(cs))
            sensorQueue.add(cs);
    }

    @Override
    public void run() {
        // grab a handle to this thread
        clientThread = Thread.currentThread();

        // start the sensor update thread
        SensorUpdater updater0 = new SpoofSensorUpdater(this);

        // add every sensor from inside wrapper to the updater
        for(ComparableSensor cs : sensorQueue.toArray(new ComparableSensor[sensorQueue.size()]))
            updater0.addSensor(cs.sensor());

        // kick off the updater on a separate thread
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
                        System.out.println("wait period interrupted");
                    }
                }
                else
                    System.out.println("    wait " + 0 + " nanos for sensor " + currentSensor.getLabel());

                // get and send updated value
                outgoingWriter.println(currentComparableSensor.sensor().getLabel() + "=" + currentSensor.getCurrent());

                // re-enqueue sensor for next update
                requeueComparableSensor(currentComparableSensor);
            }
        } catch (IOException e) {
            e.printStackTrace();
        } finally {
            try {
                // temporary: kill the updater and wait for it to end
                updater0.end();
                updater0Thread.join();
            } catch (InterruptedException e) {
                e.printStackTrace();
            }
        }

    }

    // allow client to finish
    public void end() { done = true; }
}
