package com.company;

import java.time.Instant;

/**
 * Created by brian on 10/18/16.
 */
class ComparableSensor implements Comparable<ComparableSensor> {
    private Sensor s = null;
    private RefreshType refresh = null;

    ComparableSensor(Sensor s, RefreshType r) {
        this.s = s;
        this.refresh = r;
    }

    @Override
    public int compareTo(ComparableSensor cs) {
        return s.nextRefresh(refresh).compareTo(cs.s.nextRefresh(cs.refresh));
    }

    public Sensor sensor() { return s; }
}
