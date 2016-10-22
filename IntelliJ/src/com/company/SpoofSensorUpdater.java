package com.company;

import java.time.Duration;
import java.time.Instant;

/**
 * Created by brian on 10/19/16.
 */
public class SpoofSensorUpdater extends SensorUpdater {
    public SpoofSensorUpdater(DataLoggerClient logger) { super(logger); }
    public SpoofSensorUpdater(DataLoggerClient logger, Sensor[] sensors) {
        super(logger);
        for(Sensor s : sensors)
            addSensor(s);
    }

    // add a sensor only if it fits the updater's type,
    // return false if and only if the sensor is of the wrong type
    @Override
    public boolean addSensor(Sensor s) {
        if(s instanceof SpoofSensor) {
            sensorQueue.add(s.asComparable(RefreshType.VALUE_UPDATE));
            return true;
        }

        return false;
    }

    @Override
    public void run() {
        while(!done) {
            // retrieve next sensor from queue
            ComparableSensor currentComparableSensor = sensorQueue.poll();
            Sensor currentSensor = currentComparableSensor.sensor();

            // wait until instant of update
            Duration negativeDelta = Duration.between(currentComparableSensor.nextRefresh(), Instant.now());
            if(negativeDelta.isNegative()) {
                long millisToWait = -1 * negativeDelta.toMillis();
                int nanosToWait = Math.max(0, -1 * (int)negativeDelta.plusMillis(millisToWait).toNanos());

                try {
                    Thread.sleep(millisToWait, nanosToWait);
                } catch (InterruptedException e) {
                    e.printStackTrace();
                }
            }

            // update sensor
            if(currentSensor.refresh()) {
                System.out.println("sensor " + currentSensor.getLabel() + " critical state change (" + currentSensor.peekCurrent() + ")");

                // sensor's critical state has changed, renew in client logger
                ownerLogger.renewSensor(currentSensor);
            }

            // re-enqueue
            sensorQueue.add(currentComparableSensor);
        }
    }
}