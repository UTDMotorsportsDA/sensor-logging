package edu.utdallas.utdmotorsports.car;

import edu.utdallas.utdmotorsports.Stoppable;
import edu.utdallas.utdmotorsports.car.Sensor.RefreshType;

import java.time.Duration;
import java.time.Instant;
import java.util.Comparator;
import java.util.PriorityQueue;
import java.util.concurrent.TimeUnit;

// this class is intended to operate any sensor without optimization from specific sensor details
// either use this or inherit from it to optimize updating
public class SensorUpdater implements Runnable, Stoppable {

    protected DataLogger ownerLogger = null;
    protected PriorityQueue<Sensor> sensorQueue;
    private boolean done = false;

    // used to wait on an empty sensor queue
    private final Object monitor = new Object();

    // allow quit() to interrupt any waiting or sleeping and exit immediately
    Thread runningThread;

    SensorUpdater(DataLogger logger) {
        // keep track in case a sensor's critical state changes
        this.ownerLogger = logger;

        // instantiate PriorityQueue with a comparator
        // to specifically check value updates
        sensorQueue = new PriorityQueue<>(new Comparator<Sensor>() {
            @Override
            public int compare(Sensor sensor, Sensor t1) {
                return sensor.nextRefresh(RefreshType.VALUE_UPDATE).compareTo(t1.nextRefresh(RefreshType.VALUE_UPDATE));
            }
        });
    }
    SensorUpdater(DataLogger logger, Sensor[] sensors) {
        this(logger);
        for(Sensor s : sensors)
            addSensor(s);
    }

    @Override
    public void run() {
        // capture reference to this thread
        runningThread = Thread.currentThread();


        try {
            while(!done) {
                // if this instance has no sensors in it, sit here and wait
                synchronized (monitor) {
                    while (sensorQueue.size() < 1) // .wait is prone to spurious wakeups, hold here if size < 1
                        monitor.wait();
                }

                // retrieve next sensor from queue
                Sensor currentSensor = sensorQueue.poll();

                // wait until moment of update
                // basically just "sleep for the right number of nanoseconds"
                // prevent negative sleep
                TimeUnit.NANOSECONDS.sleep(Math.max(0, Duration.between(Instant.now(), currentSensor.nextRefresh(RefreshType.VALUE_UPDATE)).toNanos()));

                // update sensor
                if (currentSensor.refresh()) {
                    // sensor's critical state has changed, renew in client logger
                    ownerLogger.renewSensor(currentSensor);
                }

                // re-enqueue (updater's queue is never subject to
                // noticeable outside interference)
                sensorQueue.add(currentSensor);
            }
        } catch (InterruptedException e) {
            if(!done)
                e.printStackTrace();
        }
    }

    // returns true if sensor is accepted and added, false otherwise
    public boolean addSensor(Sensor s) {
        sensorQueue.add(s);
        synchronized(monitor) { monitor.notify(); } // the update thread can wake up and check the queue
        return true;
    }

    // quit immediately
    @Override
    public final void quit() { done = true; runningThread.interrupt(); }
}
