import javax.net.ssl.SSLContext;
import java.io.File;
import java.io.FileInputStream;
import java.io.IOException;
import java.time.Duration;
import java.util.ArrayList;
import java.util.Enumeration;
import java.util.Properties;

public final class ConfigLoader {
    public static Sensor[] getSensorsFromFile(String filename) {
        ArrayList<Sensor> sensors = new ArrayList<Sensor>();
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
            if(parameters.length != 5) {
                System.err.println("config.properties error (" + sensorName + "): incorrect number of parameters (5 expected)");
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
                    try {
                        sensors.add(new SpoofSensor(sensorName, refreshPeriods, Float.parseFloat(parameters[4])));
                    } catch(NumberFormatException ex) {
                        System.err.println("config.properties error (" + sensorName + "): incorrect duration formatting (float value expected)");
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
