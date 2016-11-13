package fsae.da.car;

import java.time.Duration;
import java.time.Instant;

public class SpoofSensor extends Sensor { // fake sensor data for testing

    private float currentValue = 40.f;
    private float criticalThreshold;
    private boolean wasCritical = false;

    private boolean checkCritical(float newValue) {
        return critical; // never to be set critical internally
    }

    public SpoofSensor(String label, Duration[] timesBetweenUpdates, float criticalThreshold) {
        super(label, timesBetweenUpdates);
        this.criticalThreshold = criticalThreshold;
    }

    @Override
    public synchronized boolean refresh() {
        // random fluctuation
        float scale = 1.f;
        try {
            scale = (float)Duration.between(Instant.now(), lastRefreshes[0]).toMillis() / 1000.f;
        } catch(NullPointerException e) {}
        currentValue += ((float)Math.random() - 0.5f) * scale;
        lastRefreshes[0] = Instant.now();

        if((critical = checkCritical(currentValue)) == wasCritical) {
            wasCritical = critical;
            return false; // critical state has remained the same
        }
        else {
            wasCritical = critical;
            return true; // critical state has changed
        }
    }

    @Override
    public synchronized String peekCurrent() {
        return String.valueOf(currentValue);
    }
}
