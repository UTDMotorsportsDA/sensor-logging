package com.company;

/**
 * Created by brian on 10/18/16.
 */
public abstract class SensorUpdater implements Runnable {

    private DataLoggerClient ownerLogger = null;
    private Sensor[] sensors = null;

    @Override
    public abstract void run();

    public SensorUpdater(DataLoggerClient logger, Sensor[] sensors) {
        this.ownerLogger = logger;
        this.sensors = sensors;
    }

}
