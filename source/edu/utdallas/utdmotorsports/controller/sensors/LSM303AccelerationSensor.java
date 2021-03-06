package edu.utdallas.utdmotorsports.controller.sensors;

import edu.utdallas.utdmotorsports.DataPoint;
import edu.utdallas.utdmotorsports.controller.Sensor;

import java.time.Duration;

/**
 * Communicate with the IMU
 */

public class LSM303AccelerationSensor extends Sensor {
    public static final float ONE_G = 9.80665f;

    private int busNumber; // caller must specify the bus this device is connected to
    private final byte DEVICE_ADDRESS = 0x19; // from device datasheet
    private final byte MEASUREMENT_START_ADDRESS = (byte)0x28; // from device datasheet
    private final int BYTES_PER_MEASUREMENT = 6; // from device datasheet

    // x, y, and z values in SI units of meters per second per second
    private float conversionScaleFactor = 0.0f; // device register values need conversion to SI

    public LSM303AccelerationSensor(String label, Duration[] timesBetweenUpdates, float maximumReading, int busNumber) {
        super(label, timesBetweenUpdates);
        this.busNumber = busNumber;

        // configure sensor
        configureOptions(maximumReading, refreshPeriods[0]);
    }

    private byte[] readValue() {
        // set highest bit of register address to allow multi-byte read
        return NativeI2C.read(BYTES_PER_MEASUREMENT, (byte)((MEASUREMENT_START_ADDRESS & 0xff) | 0x80), DEVICE_ADDRESS, busNumber);
    }

    @Override
    public boolean refresh() {
        // call parent
        super.refresh();

        // pull a set of XYZ values from the accelerometer
        // record the time at which the reading was taken
        byte[] reading = readValue();
        long timestamp = System.currentTimeMillis();

        // convert from LSM303 register values a string of SI-unit values
        String value = "";
        for(int i = 0; i < 3; ++i)
             value += conversionScaleFactor * (short)((short)(((reading[2*i+1] & 0xff) << 8) | reading[2*i] & 0xff) >> 4) + ",";

        // cut off the trailing comma
        value = value.substring(0, value.length() - 1);

        // store as DataPoint
        currentDataPoint = new DataPoint(getLabel(), value, timestamp, false);

        return false;
    }

    private void configureOptions(float maxAcceleration, Duration valueRefreshInterval) {
        byte[] lowerConfigVals = new byte[0x26 - 0x20 + 1]; // start register address, ctrl registers from 0x20 to 0x26
        byte ODR, FS; // Output Data Rate, Full Scale (range) select

        // set the LSM303 to measure at least as fast as the sensor needs to update
        if(valueRefreshInterval.compareTo(Duration.ofSeconds(1)) >= 0) { // 1 Hz
            ODR = 0x01;
        }
        else if(valueRefreshInterval.compareTo(Duration.ofMillis(100)) >= 0) { // 10 Hz
            ODR = 0x02;
        }
        else if(valueRefreshInterval.compareTo(Duration.ofMillis(40)) >= 0) { // 25 Hz
            ODR = 0x03;
        }
        else if(valueRefreshInterval.compareTo(Duration.ofMillis(20)) >= 0) { // 50 Hz
            ODR = 0x04;
        }
        else if(valueRefreshInterval.compareTo(Duration.ofMillis(10)) >= 0) { // 100 Hz
            ODR = 0x05;
        }
        else if(valueRefreshInterval.compareTo(Duration.ofMillis(5)) >= 0) { // 200 Hz
            ODR = 0x06;
        }
        else if(valueRefreshInterval.compareTo(Duration.ofNanos(2500000)) >= 0) { // 400 Hz
            ODR = 0x07;
        }
        else { // 1.344 kHz max (staying in high resolution mode)
            ODR = 0x09;
            refreshPeriods[0] = Duration.ofNanos(744048); // can't possibly refresh faster than this
            if(refreshPeriods[0].compareTo(refreshPeriods[1]) < 0)
                refreshPeriods[1] = refreshPeriods[0];
            if(refreshPeriods[0].compareTo(refreshPeriods[2]) < 0)
                refreshPeriods[2] = refreshPeriods[0];
        }

        // set the range to at least as wide as the sensor needs to detect
        maxAcceleration = Math.abs(maxAcceleration);
        if(maxAcceleration <= 2 * ONE_G) {
            FS = 0x00;
            conversionScaleFactor = 0.001f * ONE_G;
        }
        else if(maxAcceleration <= 4 * ONE_G) {
            FS = 0x01;
            conversionScaleFactor = 0.002f * ONE_G;
        }
        else if(maxAcceleration <= 8 * ONE_G) {
            FS = 0x02;
            conversionScaleFactor = 0.004f * ONE_G;
        }
        else { // LSM303 goes up to 16g max measurement
            FS = 0x03;
            conversionScaleFactor = 0.012f * ONE_G;
        }

        // set sensor update rate, normal (not low-power) mode, all axes enabled
        lowerConfigVals[0] = (byte)(((ODR & 0xff) << 4) | 0x07);

        // set sensor range, high resolution mode
        lowerConfigVals[3] = (byte)(((FS & 0xff) << 4) | 0x08);

        // write determined configuration to sensor
        NativeI2C.write(lowerConfigVals, lowerConfigVals.length, (byte)0xa0, DEVICE_ADDRESS, busNumber);
    }
}
