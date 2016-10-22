package com.company;

import java.time.Duration;
import java.time.Instant;

/**
 * Created by brian on 10/16/16.
 */
public class SpoofSensor extends Sensor { // fake sensor data for testing

    private float currentValue = 40.f;
    private float criticalThreshold;

    private boolean checkCritical(float newValue) {
        if(newValue < criticalThreshold)
            return false;
        else
            return true;
    }

    public SpoofSensor(String label, Duration[] timesBetweenUpdates, float criticalThreshold) {
        super(label, timesBetweenUpdates);
        this.criticalThreshold = criticalThreshold;
    }

    @Override
    public synchronized boolean refresh() {
        boolean wasCritical = critical;

        // random fluctuation
        currentValue += (float)Math.random() - 0.5f;
        // 1% chance of large fluctuation
        if(Math.random() < 0.05f) currentValue += (float)Math.random() * 30.f * (isCritical() ? -1 : 1);
        lastRefreshes[0] = Instant.now();

        if((critical = checkCritical(currentValue)) == wasCritical)
            return false; // critical state has remained the same
        else
            return true; // critical state has changed
    }

    @Override
    public synchronized String getCurrent() {
        // consider this a refresh of pit data
        lastRefreshes[1] = Instant.now();
        return peekCurrent();
    }

    @Override
    public synchronized String peekCurrent() {
        return String.valueOf(currentValue);
    }
}
