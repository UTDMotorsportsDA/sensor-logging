package fsae.da.car;

import java.time.Duration;
import java.time.Instant;

public abstract class Sensor {
    // exact timing delays are messy,
    // but we want sample intervals to average out to the set target
    private static final double TOTAL_TIMING_ERROR_P_GAIN = 0.5;
    private static final double TOTAL_TIMING_ERROR_I_GAIN = 0.5;
    private static final double TOTAL_TIMING_ERROR_D_GAIN = 0.0; // not in use

    protected String name = null;
    protected boolean critical = false;

    // 3 elements: value update, pit, pit critical
    protected Duration[] refreshPeriods = null;
    // 2 elements: value update, pit
    protected Instant[] lastRefreshes = new Instant[2];

    // smooth out deviations from real-time update (errors caused by program overhead, non-RTOS, etc)
    // 2 elements: value update, pit update
    private Duration[] refreshErrors = {Duration.ZERO, Duration.ZERO};

    // default access: children shall overload this constructor, adding a 'criticalThreshold' parameter
    // and call super(...) to handle first 2 parameters
    Sensor(String label, Duration[] timesBetweenUpdates) throws IllegalArgumentException {
        this.name = label;
        if(timesBetweenUpdates.length != 3)
            throw new IllegalArgumentException("all sensors need 3 refresh periods");
        else {
            this.refreshPeriods = timesBetweenUpdates.clone();

            // ensure positive refresh periods
            for(Duration d : refreshPeriods)
                if(d.isNegative())
                    throw new IllegalArgumentException("refresh periods cannot be negative");

            // no reporting updates may exceed the speed of value updates
            if(refreshPeriods[1].compareTo(refreshPeriods[0]) < 0)
                refreshPeriods[1] = refreshPeriods[0];
            if(refreshPeriods[2].compareTo(refreshPeriods[0]) < 0)
                refreshPeriods[2] = refreshPeriods[0];
        }

        // initialize to make first update instantaneous
        // assume non-critical
        lastRefreshes[0] = Instant.now().minus(refreshPeriods[0]);
        lastRefreshes[1] = Instant.now().minus(refreshPeriods[1]);
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

        // handle null values gracefully
        if(lastRefreshes[typeOffset] == null)
            return Instant.now();

        // last refresh plus time until next refresh
        Instant idealComputedInstant = lastRefreshes[typeOffset].plus(refreshPeriods[(typeOffset == 0) ? 0 : (typeOffset + (isCritical() ? 1 : 0))]);

        // account for error accumulation
        Duration scaledDelta = refreshErrors[typeOffset];
        scaledDelta = scaledDelta.plus(scaledDelta.dividedBy(2));
        return idealComputedInstant.minus(scaledDelta);
    }

    // update current value, return whether 'critical' has changed
    // should be called at the beginning of child implementation
    protected boolean refresh() {
        // calculate difference between desired refresh period and realized refreshed period
        // if realized period is too long, difference is positive
        // add to accumulator to offset next reported refresh
        refreshErrors[0] = refreshErrors[0].plus(Duration.between(lastRefreshes[0], Instant.now()).minus(refreshPeriods[0]));

        // track time of most recent refresh
        lastRefreshes[0] = Instant.now();

        // this is to be ignored by child
        return false;
    }

    public abstract String peekCurrent(); // look at current value without causing refresh

    // return current value and log update time (considered a pit update)
    public String getCurrent() {
        // calculate difference between desired refresh period and realized refreshed period
        // if realized period is too long, difference is positive
        // add to accumulator to offset next reported refresh
        refreshErrors[1] = refreshErrors[1].plus(Duration.between(lastRefreshes[1], Instant.now()).minus(refreshPeriods[1]));

        lastRefreshes[1] = Instant.now();
        return peekCurrent();
    }
}
