package com.company;

/**
 * Created by brian on 10/16/16.
 */
public abstract class Sensor {
    // sensor name (e.g.: accelerometer_x_axis)
    String label = null;
    // on BeagleBone, most Sensors will have an associated pin (or similar)

    // get a sensor reading
    public abstract String getValue();

    // convenience method format & transmit data
    public String getDataPoint() {
        return label + "=" + getValue();
    }
}
