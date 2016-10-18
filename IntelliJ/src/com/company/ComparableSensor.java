package com.company;

import java.time.Instant;

/**
 * Created by brian on 10/18/16.
 */
class ComparableSensor implements Comparable<ComparableSensor> {
    private Sensor s = null;
    private boolean updateOrReport = false, pitOrDriver = false;

    ComparableSensor(Sensor s, boolean updateOrReport, boolean pitOrDriver) {
        this.s = s;
        this.updateOrReport = updateOrReport;
        this.pitOrDriver = pitOrDriver;
    }

    public Instant compareBy() {
        if(updateOrReport) // report
            return s.timeOfNextReport(pitOrDriver);
        else // update
            return s.timeOfNextUpdate();
    }

    @Override
    public int compareTo(ComparableSensor cs) {
        return this.compareBy().compareTo(cs.compareBy());
    }

    public Sensor sensor() { return s; }
}
