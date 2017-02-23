package edu.utdallas.utdmotorsports.car.sensors;

import java.util.HashMap;
import java.util.Map;

/**
 * Native interface for I2C on the BeagleBone
 */
public final class NativeI2C {
    // connect bus numbers to open file descriptors
    private static Map<Integer, Integer> fdMap;

    // no instantiation allowed
    private NativeI2C() {}

    static {
        System.loadLibrary("nativei2c");
        fdMap = new HashMap<>();
    }

    // accept start address as a parameter
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
