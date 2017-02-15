package fsae.da.car;

import java.time.Instant;

// wrap sensors in such a way that they can be sorted (such as in a priority queue)
public class ComparableSensor implements Comparable<ComparableSensor> {
    private Sensor s = null;
    private RefreshType refresh = null;

    ComparableSensor(Sensor s, RefreshType r) {
        this.s = s;
        this.refresh = r;
    }

    public Instant nextRefresh() { return s.nextRefresh(refresh); }

    @Override
    public int compareTo(ComparableSensor cs) {
        return s.nextRefresh(refresh).compareTo(cs.s.nextRefresh(cs.refresh));
    }

    public Sensor sensor() { return s; }
    public RefreshType rType() { return refresh; }
}
