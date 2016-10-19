package com.company;

import java.time.Duration;
import java.time.Instant;

/**
 * Created by brian on 10/16/16.
 */
public abstract class Sensor {
    protected String name = null;
    protected boolean critical = false;

    // value update, pit, driver, pit critical, driver critical
    protected Duration[] refreshPeriods = null;
    // value update, pit, driver
    protected Instant[] lastRefreshes = new Instant[3];

    public String getLabel() { return name; }
    public boolean isCritical() { return critical; }
    private void setCritical(boolean newState) { critical = newState; }
    public Instant nextRefresh(RefreshType rType) {
        int typeOffset = 0;
        switch (rType) {
            case VALUE_UPDATE:
                typeOffset = 0;
                break;
            case PIT:
                typeOffset = 1;
                break;
            case DRIVER:
                typeOffset = 2;
                break;
        }

        if(lastRefreshes[typeOffset] == null)
            return Instant.now();

        int criticalOffset = isCritical() ? 2 : 0;

        // last refresh plus time until next refresh
        return lastRefreshes[typeOffset].plus(refreshPeriods[(typeOffset == 0) ? 0 : typeOffset + criticalOffset]);
    }
    public ComparableSensor asComparable(RefreshType rType) { return new ComparableSensor(this, rType); }
    // various methods to stay up-to-date and retrieve value
    public abstract void refresh(); // assumed to be of type VALUE_UPDATE
    public abstract String getCurrent(RefreshType rType);
    public abstract String getRefreshed(RefreshType rType);
}

/*
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

    // update the sensor's current value (returns true if this is now in a critical state)
    public abstract boolean update();

    // return wrapper used to compare report/update times of instances
    public ComparableSensor asComparable(RefreshType r) {
        return new ComparableSensor(this, r);
    }

    // retrieve a sensor reading (make sure to synchronize in all children)
    public abstract String getValue();

    // convenience method format & transmit data
    public String getDataPoint() {
        return label + "=" + getValue();
    }

    public synchronized Duration timeUntilNextUpdate() {
        if(lastUpdate == null || updatePeriod == null)
            return Duration.ZERO;

        // return updatePeriod minus time already elapsed
        return updatePeriod.minus(Duration.between(lastUpdate, Instant.now()));
    }
}
*/