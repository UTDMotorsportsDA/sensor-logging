package com.company;

import java.awt.*;
import java.time.Duration;
import java.time.Instant;

/**
 * Created by brian on 10/16/16.
 */
// implement Comparable to allow sorting in terms of next update time
public abstract class Sensor {
    // sensor name (e.g.: accelerometer_x_axis)
    protected String label = null;
    // on BeagleBone, most Sensors will have an associated pin (or similar)
    // wrapped primitive value
    protected Object currentValue = null;

    // false: nominal, true: critical
    protected boolean criticalState = false;

    public String getLabel() { return label; }

    public synchronized boolean isCritical() { return criticalState; }

    // handle sensor-by-sensor update intervals
    // update interval is not enforced, just provided
    protected Duration updatePeriod = Duration.ofMillis(500); // default .5 s period
    protected Instant lastUpdate = null;

    protected void trackUpdate() {
        lastUpdate = Instant.now();
    }

    public synchronized Instant timeOfNextUpdate() {
        if(lastUpdate == null || updatePeriod == null)
            return Instant.now();

        // return previous update time plus update period
        return lastUpdate.plus(updatePeriod);
    }

    // reporting intervals so sensor can update fast to determine critical state
    // value order: pit nominal, pit critical, driver nominal, driver critical
    protected Duration[] reportingPeriods = null;
    // value order: pit, driver
    protected Instant[] lastReport = null;

    // isPit indicates whether to return time of report to pit or driver
    public synchronized Instant timeOfNextReport(boolean isPit) {
        if(lastReport == null || lastReport[0] == null || lastReport[1] == null)
            return Instant.now();

        // determine which period to report on
        final Duration relevantPeriod = reportingPeriods[(isPit ? 0 : 2) + (this.isCritical() ? 1 : 0)];

        // simple Instant calculation
        return lastReport[(isPit ? 0 : 1)].plus(relevantPeriod);
    }

    // return wrapper used to compare report/update times of instances
    public ComparableSensor asComparable(boolean update, boolean isPit) {
        return new ComparableSensor(this, update, isPit);
    }

    // update the sensor's current value (returns true if this is now in a critical state)
    public abstract boolean update();

    // retrieve a sensor reading (make sure to synchronize in all children)
    public abstract String getValue();

    // convenience method format & transmit data
    public String getDataPoint() {
        return label + "=" + getValue();
    }

    /*
    public synchronized Duration timeUntilNextUpdate() {
        if(lastUpdate == null || updatePeriod == null)
            return Duration.ZERO;

        // return updatePeriod minus time already elapsed
        return updatePeriod.minus(Duration.between(lastUpdate, Instant.now()));
    }
    */
}
