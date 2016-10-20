package com.company;

import java.util.concurrent.PriorityBlockingQueue;

/**
 * Created by brian on 10/18/16.
 */
public abstract class SensorUpdater implements Runnable {

    protected DataLoggerClient ownerLogger = null;
    protected PriorityBlockingQueue<ComparableSensor> sensorQueue = new PriorityBlockingQueue<>();
    protected boolean done = false;

    @Override
    public abstract void run();

    public SensorUpdater(DataLoggerClient logger, Sensor[] sensors) {
        this.ownerLogger = logger;
        for(Sensor s : sensors)
            sensorQueue.add(s.asComparable(RefreshType.VALUE_UPDATE));
    }

    public void end() { done = true; }
}
