package edu.utdallas.utdmotorsports.car.sensors;

import edu.utdallas.utdmotorsports.DataPoint;
import edu.utdallas.utdmotorsports.car.Sensor;

import java.time.Duration;

/**
 * Read 3-axis rotation data from an L3GD20H Gyroscope MEMS Motion Sensor in SI units of radians per second.
 * <p>This is one of three sensors built into <a href="https://www.adafruit.com/product/1714">Adafruit's 9-DOF IMU</a>.</p>
 * @see <a href="LSM303AccelerationSensor.html">LSM303AccelerationSensor</a>
 * @see <a href="LSM303MagneticSensor.html">LSM303MagneticSensor</a>
 */
public class L3GD20HGyroscopeSensor extends Sensor {
    public final double RADIANS_PER_DEGREE = Math.PI / 180.0;

    /**
     * Identifier of the I2C bus to which this sensor is physically connected - specified by the application
     */
    private int busNumber;

    /**
     * Static I2C address set by the manufacturer
     */
    private final byte DEVICE_ADDRESS = 0x6b;

    /**
     * Static register address set by the manufacturer
     */
    private final byte MEASUREMENT_START_ADDRESS = (byte) 0x28;

    /**
     * Static size set by the manufacturer
     */
    private final int BYTES_PER_MEASUREMENT = 6;

    /**
     * Scaling factor to convert from register byte values to SI units of radians/second - configured upon creation
     */
    private float conversionScaleFactor = 0.0f;

    /**
     * Create an instance of this Sensor object.
     *
     * @param label               passed through to <a href="../Sensor.html">Sensor</a> base class
     * @param timesBetweenUpdates passed through to <a href="../Sensor.html">Sensor</a> base class
     * @param maximumReading      maximum value expected by the application - not to be used as a critical state threshold
     * @param busNumber           identifier of the I2C bus to which this sensor is physically connected
     */
    public L3GD20HGyroscopeSensor(String label, Duration[] timesBetweenUpdates, float maximumReading, int busNumber) {
        super(label, timesBetweenUpdates);
        this.busNumber = busNumber;

        // configure sensor
        configureOptions(maximumReading, refreshPeriods[0]);
    }

    /**
     * Call I2C methods to poll the L3GD20H.
     *
     * @return raw bytes received from the L3GD20H
     */
    private byte[] readValue() {
        // set highest bit of register address to allow multi-byte read
        return NativeI2C.read(BYTES_PER_MEASUREMENT, (byte)((MEASUREMENT_START_ADDRESS & 0xff) | 0x80), DEVICE_ADDRESS, busNumber);
    }

    /**
     * Read a new value from the sensor, logging the time of measurement.
     * @return false - critical state sensing is not implemented
     */
    @Override
    public boolean refresh() {
        // call parent
        super.refresh();

        // pull a set of XYZ values from the gyro
        // record the time at which the reading was taken
        byte[] reading = readValue();
        long timestamp = System.currentTimeMillis();

        // convert from L3GD20H register values to a string of SI-unit values
        String value = "";
        for(int i = 0; i < 3; ++i)
            value += conversionScaleFactor * (short)((short)(((reading[2*i+1] & 0xff) << 8) | reading[2*i] & 0xff) >> 4) + ",";

        // cut off the trailing comma
        value = value.substring(0, value.length() - 1);

        // store as DataPoint
        currentDataPoint = new DataPoint(getLabel(), value, timestamp, false);

        return false;
    }

    /**
     * Set the configuration registers of the L3GD20H.
     * @param maxRadiansPerSec maximum rotation rate the sensor will expect on any one axis - this sets the sensor's range and resolution
     * @param valueRefreshInterval maximum allowable time between updates internal to the L3GD20H - set this to the time between value updates
     */
    private void configureOptions(float maxRadiansPerSec, Duration valueRefreshInterval) {
        byte[] lowerConfigVals = new byte[0x26 - 0x20 + 1]; // start register address, ctrl registers from 0x20 to 0x26
        byte DR, BW, FS; // Data Rate, BandWidth, Full Scale
        final byte[] ZEROES = {0, 0, 0, 0, 0, 0, 0, 0};

        // set the LSM303 to measure at least as fast as the sensor needs to update
        if(valueRefreshInterval.compareTo(Duration.ofMillis(10)) >= 0) { // 100 Hz min (staying in high data rate mode)
            DR = 0x00;
        } else if (valueRefreshInterval.compareTo(Duration.ofMillis(5)) >= 0) { // 200 Hz
            DR = 0x01;
        } else if (valueRefreshInterval.compareTo(Duration.ofNanos(2500000)) >= 0) { // 400 Hz
            DR = 0x02;
        } else { // 800 Hz max (staying in high data rate mode)
            DR = 0x03;
            refreshPeriods[0] = Duration.ofNanos(2500000); // can't possibly refresh faster than this
            if(refreshPeriods[0].compareTo(refreshPeriods[1]) < 0)
                refreshPeriods[1] = refreshPeriods[0];
            if(refreshPeriods[0].compareTo(refreshPeriods[2]) < 0)
                refreshPeriods[2] = refreshPeriods[0];
        }

        // no reason to use bandwidth option yet
        BW = 0x00;

        // set the range to at least as wide as the sensor needs to detect
        maxRadiansPerSec = Math.abs(maxRadiansPerSec);
        if(maxRadiansPerSec <= 245.0 / RADIANS_PER_DEGREE) {
            FS = 0x00;
            conversionScaleFactor = 0.00875f;
        } else if (maxRadiansPerSec <= 500.0 / RADIANS_PER_DEGREE) {
            FS = 0x01;
            conversionScaleFactor = 0.0175f;
        } else { // 2000 dps max range
            FS = 0x02;
            conversionScaleFactor = 0.07f;
        }

        // Data Rate / Bandwidth, no power down, all axes enabled
        lowerConfigVals[0] = (byte)(((DR & 0xff) << 6) | ((BW & 0xff) << 4) | 0x0f);

        // Full Scale selection
        lowerConfigVals[3] = (byte)((FS & 0xff) << 4);

        // write determined configuration to sensor
        NativeI2C.write(lowerConfigVals, lowerConfigVals.length, (byte)0xa0, DEVICE_ADDRESS, busNumber);

        // set other bytes to their default (in case device was previously configured elsewhere)
        NativeI2C.writeByte((byte)0x00, (byte)0x2e, DEVICE_ADDRESS, busNumber);
        NativeI2C.writeByte((byte)0x00, (byte)0x30, DEVICE_ADDRESS, busNumber);
        NativeI2C.write(ZEROES, 0x39 - 0x32 + 1, (byte)0x32, DEVICE_ADDRESS, busNumber);
    }
}
