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
    public boolean equals(Object cs) {
        if(cs instanceof ComparableSensor)
            return this.s.equals(((ComparableSensor)cs).s) && this.refresh == ((ComparableSensor)cs).refresh;
        else
            return false;
    }

    @Override
    public int compareTo(ComparableSensor cs) {
        return s.nextRefresh(refresh).compareTo(cs.s.nextRefresh(cs.refresh));
    }

    public Sensor sensor() { return s; }
    public RefreshType rType() { return refresh; }
}
