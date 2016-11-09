package fsae.da.car;

import java.time.Duration;

/**
 * Created by brian on 11/8/16.
 */
public class I2CSensor extends Sensor{
    private char i2cAddr;
    private char dataStartAddr;
    private int bytesPerDataPoint;
    private int[] currentValue = new int[3];

    private static int i2cFd;

    private static synchronized native void openI2Cbus(int busNum);
    private static synchronized native void setI2Cslave(char slaveAddr);
    private static synchronized native void writeBytes(char[] data, int bytesToWrite);
    private static synchronized native void readBytes(char dataStartAddr, char[] buf, int bytesToRead);

    public I2CSensor(String label, Duration[] timesBetweenUpdates, char i2cAddr, char dataStartAddr, int bytesPerDataPoint) {
        super(label, timesBetweenUpdates);
        this.i2cAddr = i2cAddr;
        this.dataStartAddr = dataStartAddr;
        this.bytesPerDataPoint = bytesPerDataPoint;
    }

    @Override
    public boolean refresh() {
        return false;
    }

    @Override
    public String getCurrent() {
        return null;
    }

    @Override
    public String peekCurrent() {
        return null;
    }
}
