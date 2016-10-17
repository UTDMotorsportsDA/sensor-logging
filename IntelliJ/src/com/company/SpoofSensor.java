package com.company;

/**
 * Created by brian on 10/16/16.
 */
public class SpoofSensor extends Sensor { // fake sensor data for testing
    public SpoofSensor(String label) {
        this.label = label;
    }

    public String getValue() {
        return Float.toString((float)Math.random() * 100.f);
    }
}
