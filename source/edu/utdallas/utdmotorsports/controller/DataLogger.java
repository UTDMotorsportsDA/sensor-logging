package edu.utdallas.utdmotorsports.controller;

import edu.utdallas.utdmotorsports.DataPoint;
import edu.utdallas.utdmotorsports.Stoppable;
import edu.utdallas.utdmotorsports.controller.Sensor.RefreshType;

import java.time.Duration;
import java.time.Instant;
import java.util.ArrayList;
import java.util.Comparator;
import java.util.PriorityQueue;
import java.util.Queue;
import java.util.concurrent.TimeUnit;

class DataLogger implements Runnable, Stoppable {

    private PriorityQueue<Sensor> sensorQueue;
    private Queue<DataPoint> outputQueue;
    private boolean done = false;
    private Thread runningThread = null;

    DataLogger(ArrayList<Sensor> sensors, Queue<DataPoint> outputQueue) {
        this.outputQueue = outputQueue;

        // create with comparator that strictly checks logging updates
        sensorQueue = new PriorityQueue<>(new Comparator<Sensor>() {
            @Override
            public int compare(Sensor sensor, Sensor t1) {
                return sensor.nextRefresh(RefreshType.LOGGING_UPDATE).compareTo(t1.nextRefresh(RefreshType.LOGGING_UPDATE));
            }
        });

        // enqueue every sensor
        for(Sensor s : sensors) {
            sensorQueue.add(s);
        }
    }

    // intended to inform this instance of a new update interval immediately
    // removes sensor if possible and adds; different from requeueSensor
    synchronized void renewSensor(Sensor s) {
        // if sensor is in queue, remove
        sensorQueue.remove(s);

        // add sensor to queue (if sensor was in queue, moves it to the new update period)
        sensorQueue.add(s);

        // kick the logger out of its current wait period to poll for next sensor
        // if a sensor has gone critical, it needs to be handled ASAP
        runningThread.interrupt();
    }

    // part of run()'s queue'd cycle put into a method for thread synchronization
    private synchronized void requeueSensor(Sensor s) {
        // only re-add sensor if it wasn't added by a call to renewSensor
        if(!sensorQueue.contains(s))
            sensorQueue.add(s);
    }

    @Override
    public void run() {
        // grab a handle to this thread
        runningThread = Thread.currentThread();

        // create specific updaters, make the first updater a catch-all
        SensorUpdater[] updaters = {new SensorUpdater(this)};

        // add every sensor to the list of updaters
        // if sensor can't be placed in any specific updater, place it in the default
        // if sensor can't even go in the default updater, log as an error and move on
        for(Sensor s : sensorQueue) {
            int updaterIndex = updaters.length;
            while(updaterIndex > 0 && !updaters[--updaterIndex].addSensor(s));
            if(updaterIndex < 0)
                System.err.println("unable to add sensor \"" + s.getLabel() + "\" to any updater");
        }

        // kick off the updaters on separate threads
        Thread[] updaterThreads = new Thread[updaters.length];
        for(int i = 0; i < updaters.length; i++) {
            updaterThreads[i] = new Thread(updaters[i]);
            updaterThreads[i].start();
        }

        try {
            while(!done) {
                // retrieve the next sensor from which to read
                Sensor currentSensor = sensorQueue.poll();

                // wait until moment of update
                // basically just "sleep for the right number of nanoseconds"
                // prevent negative sleep
                TimeUnit.NANOSECONDS.sleep(Math.max(0, Duration.between(Instant.now(), currentSensor.nextRefresh(RefreshType.LOGGING_UPDATE)).toNanos()));

                // enqueue the most current data point for the sensor
                outputQueue.add(currentSensor.getCurrent());

                // re-enqueue sensor for next update
                requeueSensor(currentSensor);
            }
        } catch (InterruptedException e) {
            if(!done)
                e.printStackTrace();
        }

        try {
            for(SensorUpdater u : updaters)
                u.quit();
            for(Thread t : updaterThreads)
                t.join();
        } catch (InterruptedException e) {
            e.printStackTrace();
        }
    }

    // finish up and self-terminate
    @Override
    public void quit() { done = true; runningThread.interrupt(); }
}
