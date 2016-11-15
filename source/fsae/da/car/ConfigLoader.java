package fsae.da.car;

import java.io.File;
import java.io.FileInputStream;
import java.io.IOException;
import java.time.Duration;
import java.util.ArrayList;
import java.util.Enumeration;
import java.util.Properties;

public final class ConfigLoader {
    public static Sensor[] getSensorsFromFile(String filename) {
        ArrayList<Sensor> sensors = new ArrayList<>();
        Properties props = new Properties();

        // load configuration file
        try {
            props.load(new FileInputStream(new File(filename)));
        } catch (IOException e) {
            e.printStackTrace();
        }

        // for every property in the file...
        Enumeration e = props.propertyNames();

        iterateProperties:
        while(e.hasMoreElements()) {
            String sensorName = (String)e.nextElement();
            String[] parameters = props.getProperty(sensorName).split(", *");
            Duration[] refreshPeriods = new Duration[3];

            // validate parameter count
            if(parameters.length < 4) {
                System.err.println("config.properties error (" + sensorName + "): incorrect number of parameters (at least 4 needed)");
                continue iterateProperties;
            }

            // parse refresh periods in ms
            for(int i = 0; i < 3; ++i)
                try {
                    refreshPeriods[i] = Duration.ofMillis(Integer.parseInt(parameters[i + 1]));
                } catch(NumberFormatException ex) {
                    System.err.println("config.properties error (" + sensorName + "): incorrect duration formatting (integer in milliseconds expected)");
                    continue iterateProperties;
                }

            // create sensor of specified type and add to arrayList
            switch (parameters[0]) {
                case "Spoof":
                    if(parameters.length != 5) {
                        System.err.println("config.properties error (" + sensorName + "): incorrect number of parameters (Spoof type requires exactly 5)");
                        continue iterateProperties;
                    }
                    try {
                        sensors.add(new SpoofSensor(sensorName, refreshPeriods, Float.parseFloat(parameters[4])));
                    } catch(NumberFormatException ex) {
                        System.err.println("config.properties error (" + sensorName + "): incorrect formatting (float value expected)");
                        continue iterateProperties;
                    }
                    break;

                case "LSM303a":
                    if(parameters.length != 6) {
                        System.err.println("config.properties error (" + sensorName + "): incorrect number of parameters (LSM303a type requires exactly 6)");
                        continue iterateProperties;
                    }
                    try {
                        sensors.add(new LSM303AccelerationSensor(sensorName, refreshPeriods, Float.parseFloat(parameters[4]), Integer.parseInt(parameters[5])));
                    } catch(NumberFormatException ex) {
                        System.err.println("config.properties error (" + sensorName + "): incorrect formatting");
                        continue iterateProperties;
                    }
                    break;

                case "LSM303m":
                    if(parameters.length != 6) {
                        System.err.println("config.properties error (" + sensorName + "): incorrect number of parameters (LSM303m type requires exactly 6)");
                        continue iterateProperties;
                    }
                    try {
                        sensors.add(new LSM303MagneticSensor(sensorName, refreshPeriods, Float.parseFloat(parameters[4]), Integer.parseInt(parameters[5])));
                    } catch(NumberFormatException ex) {
                        System.err.println("config.properties error (" + sensorName + "): incorrect formatting");
                        continue iterateProperties;
                    }
                    break;

                case "L3GD20H":
                    if(parameters.length != 6) {
                        System.err.println("config.properties error (" + sensorName + "): incorrect number of parameters (L3GD20H type requires exactly 6)");
                        continue iterateProperties;
                    }
                    try {
                        sensors.add(new L3GD20HGyroscopeSensor(sensorName, refreshPeriods, Float.parseFloat(parameters[4]), Integer.parseInt(parameters[5])));
                    } catch(NumberFormatException ex) {
                        System.err.println("config.properties error (" + sensorName + "): incorrect formatting");
                        continue iterateProperties;
                    }
                    break;

                default:
                    System.err.println("config.properties error (" + sensorName + "): " + parameters[0] + " is not a valid sensor type");
                    continue iterateProperties;
            }
        }

        return sensors.toArray(new Sensor[0]);
    }
}
