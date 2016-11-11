package fsae.da.car;

import java.time.Duration;
import java.time.Instant;

/**
 * Created by brian on 11/9/16.
 * this implementation is sub-optimal for platform, needs:
 *      critical state sensing
 *      more generalized configuration options
 */

public class LSM303AccelerationSensor extends Sensor {
    private static final double ONE_G = 9.80665;

    private int busNumber;
    private byte deviceAddress = 0x19;
    private byte measurementStartAddress = (byte)0xa8;
    private int bytesPerMeasurement = 6;

    // x, y, and z values in SI units of meters per second per second
    private double[] currentValue = new double[3];

    public LSM303AccelerationSensor(String label, Duration[] timesBetweenUpdates, int busNumber) {
        super(label, timesBetweenUpdates);
        this.busNumber = busNumber;

        // write to configuration registers
        NativeI2C.writeByte((byte)0x57, (byte)0x20, deviceAddress, busNumber); // 100 Hz, all axes enabled
        NativeI2C.writeByte((byte)0x00, (byte)0x23, deviceAddress, busNumber); // +- 4g range
    }

    private byte[] readValue() {
        return NativeI2C.read(bytesPerMeasurement, measurementStartAddress, deviceAddress, busNumber);
    }

    @Override
    public boolean refresh() {
        short x, y, z;
        byte[] reading = readValue();

        // convert from LSM303 register values to SI units
        for(int i = 0; i < 3; ++i)
            currentValue[i] = 0.001 * (short)((short)(((reading[2*i+1] & 0xff) << 8) | reading[2*i] & 0xff) >> 4);

        return false;
    }

    @Override
    public String peekCurrent() {
        return currentValue[0] + "," + currentValue[1] + "," + currentValue[2];
    }
}
