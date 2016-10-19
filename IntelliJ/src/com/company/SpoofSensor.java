package com.company;

import java.time.Duration;
import java.time.Instant;

/**
 * Created by brian on 10/16/16.
 */
public class SpoofSensor extends Sensor { // fake sensor data for testing

    private float currentValue = 0.f;

    public SpoofSensor(String label) {
        this.name = label;
    }

    public SpoofSensor(String label, Duration[] timesBetweenUpdates) {
        this(label);
        this.refreshPeriods = timesBetweenUpdates.clone();
    }

    public synchronized void refresh() {
        currentValue = (float)Math.random() * 100.f;
        lastRefreshes[0] = Instant.now();
    }

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

    public synchronized String getRefreshed(RefreshType r) {
        refresh();
        return getCurrent(r);
    }
}
