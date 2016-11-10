package fsae.da.car;

import java.time.Duration;

/**
 * Created by brian on 11/9/16.
 */
public class I2CSensor extends Sensor {


    I2CSensor(String label, Duration[] timesBetweenUpdates, int busNumber, byte deviceAddress, ) {
        super(label, timesBetweenUpdates);
    }

    @Override
    public boolean refresh() {
        return false;
    }

    @Override
    public String getCurrent() {
        return null;
    }

    @Override
    public String peekCurrent() {
        return null;
    }
}
