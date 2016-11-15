package fsae.da.car;

import java.time.Duration;

// outputs magnetic vector in micro-Tesla
public class LSM303MagneticSensor extends Sensor {

    private int busNumber;
    private byte deviceAddress = 0x1e;
    private byte measurementStartAddress = 0x03;
    private int bytesPerMeasurement = 6;

    // x, y, and z values in SI units of meters per second per second
    private float[] currentValue = new float[3];
    private float conversionScaleFactorXY, conversionScaleFactorZ;

    public LSM303MagneticSensor(String label, Duration[] timesBetweenUpdates, float maximumReading, int busNumber) {
        super(label, timesBetweenUpdates);
        this.busNumber = busNumber;

        // configure sensor
        configureOptions(maximumReading, refreshPeriods[0]);
    }

    private byte[] readValue() {
        // set highest bit of register address to allow multi-byte read
        return NativeI2C.read(bytesPerMeasurement, measurementStartAddress, deviceAddress, busNumber);
    }


    @Override
    public boolean refresh() {
        byte[] reading = readValue();

        // convert from LSM303 register values to SI units
        for(int i = 0; i < 2; ++i)
            currentValue[i] = conversionScaleFactorXY * (short)(((reading[2*i] & 0xff) << 8) | reading[2*i+1] & 0xff);
        currentValue[2] = conversionScaleFactorZ * (short)(((reading[4] & 0xff) << 8) | reading[5] & 0xff);

        return false;
    }

    @Override
    public String peekCurrent() {
        return currentValue[0] + "," + currentValue[1] + "," + currentValue[2];
    }

    private void configureOptions(float maxField, Duration valueRefreshInterval) {
        byte[] lowerConfigVals = new byte[0x02 - 0x00 + 1]; // start register address, ctrl registers from 0x00 to 0x02
        byte DO, GN; // Data Output rate, GaiN configuration

        // set the LSM303 to measure at least as fast as the sensor needs to update
        if(valueRefreshInterval.compareTo(Duration.ofMillis(1334)) >= 0) { // 0.75 Hz
            DO = 0x00;
        }
        else if(valueRefreshInterval.compareTo(Duration.ofMillis(667)) >= 0) { // 1.5 Hz
            DO = 0x01;
        }
        else if(valueRefreshInterval.compareTo(Duration.ofMillis(334)) >= 0) { // 3.0 Hz
            DO = 0x02;
        }
        else if(valueRefreshInterval.compareTo(Duration.ofMillis(134)) >= 0) { // 7.5 Hz
            DO = 0x03;
        }
        else if(valueRefreshInterval.compareTo(Duration.ofMillis(67)) >= 0) { // 15 Hz
            DO = 0x04;
        }
        else if(valueRefreshInterval.compareTo(Duration.ofMillis(34)) >= 0) { // 30 Hz
            DO = 0x05;
        }
        else if(valueRefreshInterval.compareTo(Duration.ofMillis(14)) >= 0) { // 75 Hz
            DO = 0x06;
        }
        else { // 220 Hz, max sensing rate
            DO = 0x07;
            refreshPeriods[0] = Duration.ofNanos(4545455); // can't possibly refresh faster than this
            if(refreshPeriods[0].compareTo(refreshPeriods[1]) < 0)
                refreshPeriods[1] = refreshPeriods[0];
            if(refreshPeriods[0].compareTo(refreshPeriods[2]) < 0)
                refreshPeriods[2] = refreshPeriods[0];
        }

        // set the range to at least as wide as the sensor needs to detect
        // output is in micro-Tesla
        maxField = Math.abs(maxField);
        if(maxField <= 1.3) { // +- 1.3 gauss
            GN = 0x01;
            conversionScaleFactorXY = (float)(100.0 / 1100.0);
            conversionScaleFactorZ = (float)(100.0 / 980.0);
        }
        else if(maxField <= 1.9) { // +- 1.9 gauss
            GN = 0x02;
            conversionScaleFactorXY = (float)(100.0 / 855.0);
            conversionScaleFactorZ = (float)(100.0 / 760.0);
        }
        else if(maxField <= 2.5) { // +- 2.5 gauss
            GN = 0x03;
            conversionScaleFactorXY = (float)(100.0 / 670.0);
            conversionScaleFactorZ = (float)(100.0 / 600.0);
        }
        else if(maxField <= 4.0) { // +- 4.0 gauss
            GN = 0x04;
            conversionScaleFactorXY = (float)(100.0 / 450.0);
            conversionScaleFactorZ = (float)(100.0 / 400.0);
        }
        else if(maxField <= 4.7) { // +- 4.7 gauss
            GN = 0x05;
            conversionScaleFactorXY = (float)(100.0 / 400.0);
            conversionScaleFactorZ = (float)(100.0 / 355.0);
        }
        else if(maxField <= 5.6) { // +- 5.6 gauss
            GN = 0x06;
            conversionScaleFactorXY = (float)(100.0 / 330.0);
            conversionScaleFactorZ = (float)(100.0 / 295.0);
        }
        else { // LSM303 goes up to +- 8.1 gauss max measurement
            GN = 0x07;
            conversionScaleFactorXY = (float)(100.0 / 230.0);
            conversionScaleFactorZ = (float)(100.0 / 205.0);
        }

        // set sensor update rate
        lowerConfigVals[0] = (byte)((DO & 0xff) << 2);

        // set sensor range (gain settings)
        lowerConfigVals[1] = (byte)((GN & 0xff) << 5);

        // continuous-conversion (not sleep) mode
        lowerConfigVals[2] = 0x00;

        // write determined configuration to sensor
        NativeI2C.write(lowerConfigVals, lowerConfigVals.length, (byte)0x00, deviceAddress, busNumber);
    }
}