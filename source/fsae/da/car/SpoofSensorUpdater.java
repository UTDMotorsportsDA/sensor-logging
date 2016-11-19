package fsae.da.car;

// extending base SensorUpdater class is optional, but potentially opens up optimization
public class SpoofSensorUpdater extends SensorUpdater {
    public SpoofSensorUpdater(DataLogger logger) { super(logger); }
    public SpoofSensorUpdater(DataLogger logger, Sensor[] sensors) { super(logger, sensors); }

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
