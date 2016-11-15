package fsae.da.car;

import java.time.Duration;
import java.util.Arrays;

/**
 * Created by brian on 11/14/16.
 */
public class L3GD20HGyroscopeSensor extends Sensor {
    public final double RADIANS_PER_DEGREE = Math.PI / 180.0;

    private int busNumber;
    private byte deviceAddress = 0x6b;
    private byte measurementStartAddress = (byte)0x28;
    private int bytesPerMeasurement = 6;

    // x, y, and z values in SI units of meters per second per second
    private float[] currentValue = new float[3];
    private float conversionScaleFactor = 0.0f;

    public L3GD20HGyroscopeSensor(String label, Duration[] timesBetweenUpdates, float maximumReading, int busNumber) {
        super(label, timesBetweenUpdates);
        this.busNumber = busNumber;

        // configure sensor
        configureOptions(maximumReading, refreshPeriods[0]);
    }

    private byte[] readValue() {
        // set highest bit of register address to allow multi-byte read
        return NativeI2C.read(bytesPerMeasurement, (byte)((measurementStartAddress & 0xff) | 0x80), deviceAddress, busNumber);
    }

    @Override
    public boolean refresh() {
        byte[] reading = readValue();

        System.out.println(String.format("[ %02xh %02xh %02xh %02xh %02xh %02xh ]", reading[0], reading[1], reading[2], reading[3], reading[4], reading[5]));

        // convert from L3GD20H register values to SI units
        for(int i = 0; i < 3; ++i)
            currentValue[i] = conversionScaleFactor * (short)((short)(((reading[2*i+1] & 0xff) << 8) | reading[2*i] & 0xff) >> 4);

        return false;
    }

    @Override
    public String peekCurrent() {
        return currentValue[0] + "," + currentValue[1] + "," + currentValue[2];
    }

    private void configureOptions(float maxRadiansPerSec, Duration valueRefreshInterval) {
        byte[] lowerConfigVals = new byte[0x26 - 0x20 + 1]; // start register address, ctrl registers from 0x20 to 0x26
        byte DR, BW, FS; // Data Rate, BandWidth, Full Scale
        final byte[] ZEROES = {0, 0, 0, 0, 0, 0, 0, 0};

        // set the LSM303 to measure at least as fast as the sensor needs to update
        if(valueRefreshInterval.compareTo(Duration.ofMillis(10)) >= 0) { // 100 Hz min (staying in high data rate mode)
            DR = 0x00;
        }
        else if(valueRefreshInterval.compareTo(Duration.ofMillis(5)) >= 0) { // 200 Hz
            DR = 0x01;
        }
        else if(valueRefreshInterval.compareTo(Duration.ofNanos(2500000)) >= 0) { // 400 Hz
            DR = 0x02;
        }
        else { // 800 Hz max (staying in high data rate mode)
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
        }
        else if(maxRadiansPerSec <= 500.0 / RADIANS_PER_DEGREE) {
            FS = 0x01;
            conversionScaleFactor = 0.0175f;
        }
        else { // 2000 dps max range
            FS = 0x02;
            conversionScaleFactor = 0.07f;
        }

        // Data Rate / Bandwidth, no power down, all axes enabled
        lowerConfigVals[0] = (byte)(((DR & 0xff) << 6) | ((BW & 0xff) << 4) | 0x0f);

        // Full Scale selection
        lowerConfigVals[3] = (byte)((FS & 0xff) << 4);

        // write determined configuration to sensor
        NativeI2C.write(lowerConfigVals, lowerConfigVals.length, (byte)0xa0, deviceAddress, busNumber);

        // set other bytes to their default (in case previously in use elsewhere)
        NativeI2C.writeByte((byte)0x00, (byte)0x2e, deviceAddress, busNumber);
        NativeI2C.writeByte((byte)0x00, (byte)0x30, deviceAddress, busNumber);
        NativeI2C.write(ZEROES, 0x39 - 0x32 + 1, (byte)0x32, deviceAddress, busNumber);
    }
}
