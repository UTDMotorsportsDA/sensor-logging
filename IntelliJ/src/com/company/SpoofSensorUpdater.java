package com.company;

/**
 * Created by brian on 10/19/16.
 */
public class SpoofSensorUpdater extends SensorUpdater {
    public SpoofSensorUpdater(DataLoggerClient logger, Sensor[] sensors) {
        super(logger, sensors);
    }

    @Override
    public void run() {
        while(!done) {
            
        }
    }
}
