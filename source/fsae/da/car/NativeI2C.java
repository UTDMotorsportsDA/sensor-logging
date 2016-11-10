/**
 * Created by brian on 11/9/16.
 */
public final class NativeI2C {
    private NativeI2C() {}

    static {
        System.loadLibrary("nativei2c");
    }

    public static synchronized native int openDevice(int deviceNumber);
    public static synchronized native int setSlave(byte slaveAddress, int deviceFileDescriptor);
    public static synchronized        int write(byte[] bytes, int deviceFileDescriptor)
        { return writeBytes(bytes, bytes.length, deviceFileDescriptor); }
    public static synchronized native int writeByte(byte outByte, int deviceFileDescriptor);
    public static synchronized native int read(byte[] buffer, int deviceFileDescriptor);
    public static synchronized native int readByte(int deviceFileDescriptor);

    private static synchronized native int writeBytes(byte[] bytes, int numBytes, int deviceFileDescriptor);
}
