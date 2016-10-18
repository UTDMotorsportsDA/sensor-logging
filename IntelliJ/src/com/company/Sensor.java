package com.company;

import java.awt.*;
import java.time.Duration;
import java.time.Instant;

/**
 * Created by brian on 10/16/16.
 */
// implement Comparable to allow sorting in terms of next update time
public abstract class Sensor implements Comparable<Sensor> {
    // sensor name (e.g.: accelerometer_x_axis)
    protected String label = null;
    // on BeagleBone, most Sensors will have an associated pin (or similar)

    public String getLabel() { return label; }

    // handle sensor-by-sensor update intervals
    // update interval is not enforced, just provided
    protected Duration updatePeriod = Duration.ofMillis(500); // default .5 s period
    protected Instant lastUpdate = null;
    protected void trackUpdate() {
        lastUpdate = Instant.now();
    }
    public Instant timeOfNextUpdate() {
        if(lastUpdate == null || updatePeriod == null)
            return Instant.now();

        // return previous update time plus update period
        return lastUpdate.plus(updatePeriod);
    }
    public Duration timeUntilNextUpdate() {
        if(lastUpdate == null || updatePeriod == null)
            return Duration.ZERO;

        // return updatePeriod minus time already elapsed
        return updatePeriod.minus(Duration.between(lastUpdate, Instant.now()));
    }
    // "which sensor needs to update sooner?" (meant to sort ascending from soonest -> least soon)
    public int compareTo(Sensor s) { return timeOfNextUpdate().compareTo(s.timeOfNextUpdate()); }

    // get a sensor reading
    public abstract String getValue();

    // convenience method format & transmit data
    public String getDataPoint() {
        return label + "=" + getValue();
    }
}
