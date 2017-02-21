package edu.utdallas.utdmotorsports.car.sensors;

import edu.utdallas.utdmotorsports.DataPoint;
import edu.utdallas.utdmotorsports.car.Sensor;

import java.time.Duration;
import java.time.Instant;
/**
 * testing utility
 */
public class SpoofSensor extends Sensor { // fake sensor data for testing

    private float currentValue = 40.f;

    public SpoofSensor(String label, Duration[] timesBetweenUpdates) {
        super(label, timesBetweenUpdates);
    }

    @Override
    public synchronized boolean refresh() {
        // call parent
        super.refresh();

        // random fluctuation
        float scale = 1.f;
        try {
            scale = (float)Duration.between(Instant.now(), lastRefreshes[0]).toMillis() / 1000.f;
        } catch(NullPointerException e) {}
        currentValue += ((float)Math.random() - 0.5f) * scale;

        // critical state is permanently false
        return false;
    }

    @Override
    public synchronized DataPoint peekCurrent() {
        currentDataPoint = new DataPoint(getLabel(), Float.toString(currentValue), Instant.now().toEpochMilli(), false);
        return currentDataPoint;
    }
}
