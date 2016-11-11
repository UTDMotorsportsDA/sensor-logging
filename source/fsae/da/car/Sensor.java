package fsae.da.car;

import java.time.Duration;
import java.time.Instant;

public abstract class Sensor {
    protected String name = null;
    protected boolean critical = false;

    // value update, pit, pit critical
    protected Duration[] refreshPeriods = null;
    // value update, pit
    protected Instant[] lastRefreshes = new Instant[2];

    // default access: children shall overload this constructor, adding a 'criticalThreshold' parameter
    // and call super(...) to handle first 2 parameters
    Sensor(String label, Duration[] timesBetweenUpdates) throws IllegalArgumentException {
        this.name = label;
        if(timesBetweenUpdates.length != 3)
            throw new IllegalArgumentException("all sensors need 3 refresh periods (see Sensor.java)");
        else {
            this.refreshPeriods = timesBetweenUpdates.clone();

            // no reporting updates may exceed the speed of value updates
            if(refreshPeriods[1].compareTo(refreshPeriods[0]) < 0)
                refreshPeriods[1] = refreshPeriods[0];
            if(refreshPeriods[2].compareTo(refreshPeriods[0]) < 0)
                refreshPeriods[2] = refreshPeriods[0];
        }

    }

    public String getLabel() { return name; }
    public boolean isCritical() { return critical; }

    public ComparableSensor asComparable(RefreshType rType) { return new ComparableSensor(this, rType); }

    public Instant nextRefresh(RefreshType rType) {
        int typeOffset = 0;
        switch (rType) {
            case VALUE_UPDATE:
                typeOffset = 0;
                break;
            case PIT:
                typeOffset = 1;
                break;
            default:
                typeOffset = 0;
                break;
        }

        if(lastRefreshes[typeOffset] == null)
            return Instant.now();

        // last refresh plus time until next refresh
        return lastRefreshes[typeOffset].plus(refreshPeriods[(typeOffset == 0) ? 0 : (typeOffset + (isCritical() ? 1 : 0))]);
    }
    public abstract boolean refresh(); // update current value, return whether 'critical' has changed
    public abstract String peekCurrent(); // look at current value without causing refresh

    // return current value and log update time
    public String getCurrent() {
        lastRefreshes[1] = Instant.now();
        return peekCurrent();
    }
}
