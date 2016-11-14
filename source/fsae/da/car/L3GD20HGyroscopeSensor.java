package fsae.da.car;

import java.time.Duration;

/**
 * Created by brian on 11/14/16.
 */
public class L3GD20HGyroscopeSensor extends Sensor {
    public L3GD20HGyroscopeSensor(String label, Duration[] timesBetweenUpdates) {
        super(label, timesBetweenUpdates);
        // ...
    }

    @Override
    public boolean refresh() {
        return false;
    }

    @Override
    public String peekCurrent() {
        return null;
    }
}
