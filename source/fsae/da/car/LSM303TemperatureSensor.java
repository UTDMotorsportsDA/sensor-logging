package fsae.da.car;

import java.time.Duration;

/**
 * Included simply because it is available on-chip
 */
public class LSM303TemperatureSensor extends Sensor {
    public LSM303TemperatureSensor(String label, Duration[] timesBetweenUpdates) {
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
