package edu.utdallas.utdmotorsports.car.sensors;

import java.util.HashMap;
import java.util.Map;

/**
 * Native interface for I2C on the BeagleBone - requires libnativeI2C.so
 */
public final class NativeI2C {
    /**
     * Register previously-opened I2C buses to avoid unnecessary creation deletion.
     */
    private static Map<Integer, Integer> fdMap;

    /**
     * Prevent multiple instantiation (NativeI2C is a singleton).
     */
    private NativeI2C() {}

    // load external library object
    static {
        System.loadLibrary("nativei2c");
        fdMap = new HashMap<>();
    }

    /**
     * Send a byte array over I2C
     *
     * @param bytes                Bytes to be sent
     * @param numBytes             Number of bytes to send
     * @param slaveAddress         I2C address of the intended receiver
     * @param registerStartAddress Receiver's register address to store incoming data
     * @param deviceNumber         Identifier for the physical bus over which to send data
     * @return 0 for success, -1 for failure
     */
    public static synchronized int write(byte[] bytes, int numBytes, byte registerStartAddress, byte slaveAddress, int deviceNumber) {
        int fd = fileDescriptorOf(deviceNumber);
        byte[] wbuf = new byte[numBytes + 1];

        wbuf[0] = registerStartAddress;
        System.arraycopy(bytes, 0, wbuf, 1, numBytes);

        n_setSlave(slaveAddress, fd);
        return n_write(wbuf, wbuf.length, fd);
    }

    public static synchronized int writeByte(byte data, byte registerAddress, byte slaveAddress, int deviceNumber) {
        int fd = fileDescriptorOf(deviceNumber);
        final byte[] wbuf = {registerAddress, data};
        n_setSlave(slaveAddress, fd);
        return n_write(wbuf, 2, fd);
    }

    public static synchronized byte[] read(int numBytes, byte startAddress, byte slaveAddress, int deviceNumber) {
        int fd = fileDescriptorOf(deviceNumber);
        byte[] rbuf = new byte[numBytes];
        n_setSlave(slaveAddress, fd);
        n_writeByte(startAddress, fd);
        n_read(rbuf, fd);
        return rbuf;
    }

    public static synchronized int readByte(byte address, byte slaveAddress, int deviceNumber) {
        int fd = fileDescriptorOf(deviceNumber);
        n_setSlave(slaveAddress, fd);
        n_writeByte(address, fd);
        return n_readByte(fd);
    }

    private static int fileDescriptorOf(int deviceNumber) {
        Integer fd;
        if((fd = fdMap.get(deviceNumber)) == null) {
            fd = openDevice(deviceNumber);
        }
        return fd;
    }

    private static Integer openDevice(int deviceNumber) throws IllegalArgumentException {
        Integer fd;

        if ((fd = n_openDevice(deviceNumber)) < 0)
            throw new IllegalArgumentException("/dev/i2c-" + deviceNumber + " could not be opened.");

        fdMap.put(deviceNumber, fd);

        return fd;
    }

    private static native int n_openDevice(int deviceNumber);
    private static native int n_setSlave(byte slaveAddress, int deviceFileDescriptor);
    private static native int n_write(byte[] bytes, int numBytes, int deviceFileDescriptor);
    private static native int n_writeByte(byte outByte, int deviceFileDescriptor);
    private static native int n_read(byte[] buffer, int deviceFileDescriptor);
    private static native int n_readByte(int deviceFileDescriptor);
}
