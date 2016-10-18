package com.company;

import java.time.Duration;

/**
 * Created by brian on 10/16/16.
 */
public class SpoofSensor extends Sensor { // fake sensor data for testing

    public SpoofSensor(String label) {
        this.label = label;
    }

    public SpoofSensor(String label, Duration timeBetweenUpdates) {
        this(label);
        this.updatePeriod = timeBetweenUpdates;
    }

    public String getValue() {
        trackUpdate(); // takes care of interval timing
        return Float.toString((float)Math.random() * 100.f);
    }
}
