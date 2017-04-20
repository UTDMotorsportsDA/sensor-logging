package edu.utdallas.utdmotorsports.controller.sensors;

import edu.utdallas.utdmotorsports.DataPoint;
import edu.utdallas.utdmotorsports.controller.Sensor;

/**
 * simulate a sensor with a 3D vector value in each data point
 */
public class SimulatedSensorVecFloat extends Sensor {

    public SimulatedSensorVecFloat(String label, java.time.Duration[] timesBetweenUpdates, float[] seed) throws IllegalArgumentException {
        super(label, timesBetweenUpdates);
        String seedString = Float.toString(seed[0]);
        for(int i = 1; i < seed.length; ++i)
            seedString += "," + Float.toString(seed[i]);
        currentDataPoint = new DataPoint(getLabel(), seedString, System.currentTimeMillis(), false);
    }

    @Override
    public boolean refresh() {
        super.refresh();
        String[] oldVal = currentDataPoint.getValue().split(",");
        String newVal = Float.toString(Float.parseFloat(oldVal[0]) * (1.f + (float)Math.random() / 10.f - .05f));
        for(int i = 1; i < oldVal.length; ++i)
            newVal += "," + Float.toString(Float.parseFloat(oldVal[i]) * (1.f + (float)Math.random() / 10.f - .05f));
        currentDataPoint = new DataPoint(getLabel(), newVal, System.currentTimeMillis(), false);
        return false;
    }
}
