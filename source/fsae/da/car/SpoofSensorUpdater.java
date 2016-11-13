package fsae.da.car;

import java.time.Duration;
import java.time.Instant;

// extending base SensorUpdater class is optional, but potentially opens up optimization
public class SpoofSensorUpdater extends SensorUpdater {
    public SpoofSensorUpdater(DataLoggerClient logger) { super(logger); }
    public SpoofSensorUpdater(DataLoggerClient logger, Sensor[] sensors) { super(logger, sensors); }

    // add a sensor only if it fits the updater's type,
    // return false if and only if the sensor is of the wrong type
    @Override
    public boolean addSensor(Sensor s) {
        if(s instanceof SpoofSensor) {
            return super.addSensor(s);
        }

        return false;
    }
}
