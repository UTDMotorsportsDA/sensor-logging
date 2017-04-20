package edu.utdallas.utdmotorsports.controller.sensors;

import edu.utdallas.utdmotorsports.DataPoint;
import edu.utdallas.utdmotorsports.controller.Sensor;

/**
 * simulate a sensor with only one number value in each data point
 */
public class SimulatedSensorFloat extends Sensor {

    public SimulatedSensorFloat(String label, java.time.Duration[] timesBetweenUpdates, float seed) throws IllegalArgumentException {
        super(label, timesBetweenUpdates);
        currentDataPoint = new DataPoint(getLabel(), Float.toString(seed), System.currentTimeMillis(), false);
    }

    @Override
    public boolean refresh() {
        super.refresh();
        float oldVal = Float.parseFloat(currentDataPoint.getValue());
        float newVal = oldVal * (1.f + (float)Math.random() / 10.f - .05f);
        currentDataPoint = new DataPoint(getLabel(), Float.toString(newVal), System.currentTimeMillis(), false);
        return false;
    }
}
