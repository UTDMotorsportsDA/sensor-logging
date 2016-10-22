package com.company;

import java.time.Duration;
import java.time.Instant;

/**
 * Created by brian on 10/19/16.
 */
public class SpoofSensorUpdater extends SensorUpdater {
    public SpoofSensorUpdater(DataLoggerClient logger) { super(logger); }
    public SpoofSensorUpdater(DataLoggerClient logger, Sensor[] sensors) {
        super(logger, sensors);
    }

    @Override
    public boolean addSensor(Sensor s) {
        if(s instanceof SpoofSensor) {
            sensorQueue.add(s.asComparable(RefreshType.PIT));
            return true;
        }

        return false;
    }

    @Override
    public void run() {
        while(!done) {
            // retrieve next sensor from queue
            ComparableSensor currentComparableSensor = sensorQueue.poll();

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
            if(currentComparableSensor.sensor().refresh()) {
                System.out.println("sensor " + currentComparableSensor.sensor().getLabel() + " critical");

                // sensor's critical state has changed, renew in client logger
                ownerLogger.renewSensor(currentComparableSensor.sensor());
            }

            // re-enqueue
            sensorQueue.add(currentComparableSensor);
        }
    }
}

/*
            while(!done) {
                // retrieve a sensor from which to read out of the queue
                ComparableSensor currentComparableSensor = sensorQueue.poll();
                Sensor currentSensor = currentComparableSensor.sensor();

                // wait until time to update
                Duration negativeDelta = Duration.between(currentComparableSensor.nextRefresh(), Instant.now());
                if(negativeDelta.isNegative()) {

                    long millis = -1 * negativeDelta.toMillis();
                    int nanos = -1 * (int)negativeDelta.plusMillis(millis).toNanos();

                    System.out.println("    wait " + millis + " millis, " + nanos + " nanos for sensor " + currentComparableSensor.sensor().getLabel());
                    try {
                        Thread.sleep(millis, nanos);
                    } catch (InterruptedException e) {
                        e.printStackTrace();
                    }
                }
                else
                    System.out.println("    wait " + 0 + " nanos for sensor " + currentComparableSensor.sensor().getLabel());

                // get updated value
                outgoingWriter.println(currentComparableSensor.sensor().getLabel() + "=" + currentComparableSensor.sensor().getCurrent(currentComparableSensor.rType()));

                // re-enqueue sensor for next update
                sensorQueue.add(currentComparableSensor);
            }
 */