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
        super(label);
        this.refreshPeriods = timesBetweenUpdates.clone();
        this.criticalThreshold = criticalThreshold;
    }

    @Override
    public synchronized boolean refresh() {
        boolean wasCritical = critical;

        currentValue += ((float)Math.random() - 0.5f) * 5.f;
        lastRefreshes[0] = Instant.now();

        if((critical = checkCritical(currentValue)) == wasCritical)
            return false; // critical state has remained the same
        else
            return true; // critical state has changed
    }

    @Override
    public synchronized String getCurrent(RefreshType r) {
        switch (r) {
            case PIT:
                lastRefreshes[1] = Instant.now();
                break;
            case DRIVER:
                lastRefreshes[2] = Instant.now();
                break;
            default: // don't return a value for value_updates
                return "";
        }
        return String.valueOf(currentValue);
    }

    @Override
    public synchronized String getRefreshed(RefreshType r) {
        refresh();
        return getCurrent(r);
    }
}
