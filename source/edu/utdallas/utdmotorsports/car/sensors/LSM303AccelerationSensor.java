package edu.utdallas.utdmotorsports.car.sensors;

import edu.utdallas.utdmotorsports.DataPoint;
import edu.utdallas.utdmotorsports.car.Sensor;

import java.time.Duration;

/**
 * Read 3-axis acceleration data from an LSM303 Accelerometer/Magnetometer MEMS Motion Sensor in SI units of meters per second squared.
 * <p>This is one of three sensors built into <a href="https://www.adafruit.com/product/1714">Adafruit's 9-DOF IMU</a>.</p>
 * @see <a href="L3GD20HGyroscopeSensor.html">L3GD20HGyroscopeSensor</a>
 * @see <a href="LSM303MagneticSensor.html">LSM303MagneticSensor</a>
 */
public class LSM303AccelerationSensor extends Sensor {
    /**
     * Conversion between G-force and SI acceleration for readability
     */
    public static final float ONE_G = 9.80665f;

    /**
     * Identifier of the I2C bus to which this sensor is physically connected - specified by the application
     */
    private int busNumber;

    /**
     * Static I2C address set by the manufacturer
     */
    private final byte DEVICE_ADDRESS = 0x19;

    /**
     * Static register address set by the manufacturer
     */
    private final byte MEASUREMENT_START_ADDRESS = (byte) 0x28;

    /**
     * Static size set by the manufacturer
     */
    private final int BYTES_PER_MEASUREMENT = 6;

    /**
     * Scaling factor to convert from register byte values to SI units of meters/second^2 - configured upon creation
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
    public LSM303AccelerationSensor(String label, Duration[] timesBetweenUpdates, float maximumReading, int busNumber) {
        super(label, timesBetweenUpdates);
        this.busNumber = busNumber;

        // configure sensor
        configureOptions(maximumReading, refreshPeriods[0]);
    }

    /**
     * Call I2C methods to poll the LSM303.
     *
     * @return raw bytes received from the LSM303
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

        // pull a set of XYZ values from the accelerometer
        // record the time at which the reading was taken
        byte[] reading = readValue();
        long timestamp = System.currentTimeMillis();

        // convert from LSM303 register values a string of SI-unit values
        String value = "";
        for(int i = 0; i < 3; ++i)
            value += conversionScaleFactor * (short) ((short) (((reading[2 * i + 1] & 0xff) << 8) | reading[2 * i] & 0xff) >> 4) + ",";

        // cut off the trailing comma
        value = value.substring(0, value.length() - 1);

        // store as DataPoint
        currentDataPoint = new DataPoint(getLabel(), value, timestamp, false);

        return false;
    }

    /**
     * Set the configuration registers of the LSM303.
     * @param maxAcceleration maximum acceleration value the sensor will expect on any one axis - this sets the sensor's range and resolution
     * @param valueRefreshInterval maximum allowable time between updates internal to the LSM303 - set this to the time between value updates
     */
    private void configureOptions(float maxAcceleration, Duration valueRefreshInterval) {
        byte[] lowerConfigVals = new byte[0x26 - 0x20 + 1]; // start register address, ctrl registers from 0x20 to 0x26
        byte ODR, FS; // Output Data Rate, Full Scale (range) select

        // set the LSM303 to measure at least as fast as the sensor needs to update
        if(valueRefreshInterval.compareTo(Duration.ofSeconds(1)) >= 0) { // 1 Hz
            ODR = 0x01;
        } else if (valueRefreshInterval.compareTo(Duration.ofMillis(100)) >= 0) { // 10 Hz
            ODR = 0x02;
        } else if (valueRefreshInterval.compareTo(Duration.ofMillis(40)) >= 0) { // 25 Hz
            ODR = 0x03;
        } else if (valueRefreshInterval.compareTo(Duration.ofMillis(20)) >= 0) { // 50 Hz
            ODR = 0x04;
        } else if (valueRefreshInterval.compareTo(Duration.ofMillis(10)) >= 0) { // 100 Hz
            ODR = 0x05;
        } else if (valueRefreshInterval.compareTo(Duration.ofMillis(5)) >= 0) { // 200 Hz
            ODR = 0x06;
        } else if (valueRefreshInterval.compareTo(Duration.ofNanos(2500000)) >= 0) { // 400 Hz
            ODR = 0x07;
        } else { // 1.344 kHz max (staying in high resolution mode)
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
        } else if (maxAcceleration <= 4 * ONE_G) {
            FS = 0x01;
            conversionScaleFactor = 0.002f * ONE_G;
        } else if (maxAcceleration <= 8 * ONE_G) {
            FS = 0x02;
            conversionScaleFactor = 0.004f * ONE_G;
        } else { // LSM303 goes up to 16g max measurement
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
