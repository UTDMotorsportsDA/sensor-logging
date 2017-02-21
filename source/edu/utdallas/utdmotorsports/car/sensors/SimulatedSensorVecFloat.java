package edu.utdallas.utdmotorsports.car.sensors;

import edu.utdallas.utdmotorsports.DataPoint;
import edu.utdallas.utdmotorsports.car.Sensor;

import java.time.Duration;
import java.time.Instant;

/**
 * simulate a sensor with only one number value in each data point
 */
public class SimulatedSensorVecFloat extends Sensor {

    public SimulatedSensorVecFloat(String label, java.time.Duration[] timesBetweenUpdates, float[] seed) throws IllegalArgumentException {
        super(label, timesBetweenUpdates);
        String seedString = Float.toString(seed[0]);
        for(int i = 1; i < seed.length; ++i)
            seedString += "," + Float.toString(seed[i]);
        currentDataPoint = new DataPoint(getLabel(), Float.toString(seed[0]), Instant.now().toEpochMilli(), false);
    }

    @Override
    public boolean refresh() {
        super.refresh();
        String[] oldVal = currentDataPoint.getValue().split(",");
        String newVal = Float.toString(Float.parseFloat(oldVal[0]) * (1.f + (float)Math.random() / 10.f - .05f));
        for(int i = 1; i < oldVal.length; ++i)
            newVal += "," + Float.toString(Float.parseFloat(oldVal[i]) * (1.f + (float)Math.random() / 10.f - .05f));
        currentDataPoint = new DataPoint(getLabel(), newVal, Instant.now().toEpochMilli(), false);
        return false;
    }
}
