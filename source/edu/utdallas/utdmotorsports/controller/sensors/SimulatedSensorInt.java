package edu.utdallas.utdmotorsports.controller.sensors;

import edu.utdallas.utdmotorsports.DataPoint;
import edu.utdallas.utdmotorsports.controller.Sensor;

/**
 * simulate a sensor with only one number value in each data point
 */
public class SimulatedSensorInt extends Sensor {

    public SimulatedSensorInt(String label, java.time.Duration[] timesBetweenUpdates, int seed) throws IllegalArgumentException {
        super(label, timesBetweenUpdates);
        currentDataPoint = new DataPoint(getLabel(), Integer.toString(seed), System.currentTimeMillis(), false);
    }

    @Override
    public boolean refresh() {
        super.refresh();
        int oldVal = Integer.parseInt(currentDataPoint.getValue());
        int newVal = (int)(oldVal + (Math.random() - .5) * 10);
        currentDataPoint = new DataPoint(getLabel(), Integer.toString(newVal), System.currentTimeMillis(), false);
        return false;
    }
}
