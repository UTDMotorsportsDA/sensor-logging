package edu.utdallas.utdmotorsports.car;

import edu.utdallas.utdmotorsports.DataPoint;

import java.io.File;
import java.io.FileInputStream;
import java.io.IOException;
import java.io.InputStream;
import java.net.InetAddress;
import java.time.Duration;
import java.util.*;
import java.util.concurrent.BlockingQueue;
import java.util.concurrent.PriorityBlockingQueue;

public class CarMain {
    private static String DEFAULT_CONFIG_FILE = "config/general.prop";
    private static String DEFAULT_SENSORS_FILE = "config/sensor.prop";
    private static String TEST_SENSORS_FILE = "config/fake_sensor.prop";

    // args: parameter config file, sensor config file
    public static void main(String[] args) {
        String chosenConfigFile, chosenSensorsFile;

        // select config files (allow for specifying)
        if(args.length >= 2) {
            chosenConfigFile = args[0];
            chosenSensorsFile = args[1];
        }
        else {
            chosenConfigFile = DEFAULT_CONFIG_FILE;
            if(Arrays.asList(args).contains("-t"))
                chosenSensorsFile = TEST_SENSORS_FILE;
            else
                chosenSensorsFile = DEFAULT_SENSORS_FILE;
        }
        // filenames starting with "/" and "./" are later interpreted as being outside JAR
        if(chosenConfigFile.charAt(0) == '/' || chosenConfigFile.substring(0, 2).equals("./"))
            System.out.println("retreiving " + chosenConfigFile + " from this filesystem (not the JAR)");
        if(chosenSensorsFile.charAt(0) == '/' || chosenSensorsFile.substring(0, 2).equals("./"))
            System.out.println("retreiving " + chosenSensorsFile + " from this filesystem (not the JAR)");

        // load general configuration
        Properties props = new Properties();
        try {
            if(chosenConfigFile.charAt(0) == '/' || chosenConfigFile.substring(0, 2).equals("./"))
                // get the config file from the user's filesystem
                props.load(new FileInputStream(new File(chosenConfigFile)));
            else
                // get the config file saved in this JAR
                props.load(CarMain.class.getResourceAsStream("/" + chosenConfigFile));
        } catch (IOException e) {
            e.printStackTrace();
            System.exit(1);
        }

        // pull network parameters from general config file
        String multicastGroupName = props.getProperty("multicast_group");
        if(multicastGroupName == null) {
            System.err.println("could not find multicast_group in " + args[0]);
            System.exit(1);
        }
        String mcastPort = props.getProperty("multicast_port");
        if(mcastPort == null) {
            System.err.println("could not find multicast_port in " + args[0]);
            System.exit(1);
        }
        int multicastPort = Integer.parseInt(mcastPort);

        String serviceName = props.getProperty("service_name");
        if(serviceName == null) {
            System.err.println("could not find service_name in " + args[0]);
            System.exit(1);
        }
        String parametersPath = props.getProperty("parameters_location");
        if(parametersPath == null) {
            System.err.println("could not find parameters_location in " + args[0]);
            System.exit(1);
        }
        String svcPort = props.getProperty("server_socket_port");
        if(svcPort == null) {
            System.err.println("could not find server_socket_port in " + args[0]);
            System.exit(1);
        }
        int servicePort = Integer.parseInt(svcPort);

        // load sensors
        Sensor[] sensors;
        if(chosenSensorsFile.charAt(0) == '/' || chosenSensorsFile.substring(0, 2).equals("./"))
            // get the sensors file from the user's filesystem
            sensors = ConfigLoader.getSensorsFromFile(chosenSensorsFile);
        else
            // get the sensors file saved in this JAR
            sensors = ConfigLoader.getSensorsFromFile(CarMain.class.getResourceAsStream("/" + chosenSensorsFile));

        // sanity check
        System.out.println("Multicast Group: " + multicastGroupName + ":" + multicastPort);
        System.out.println("Config Filepath: " + chosenConfigFile);
        System.out.println("Sensors Filepath: " + chosenSensorsFile);

        // client to collect and enqueue data, transmitter to broadcast from the queue
        DataLogger logger = null;
        UDPTransmitter UDPtx = null;
        ServiceDiscoveryResponder SDR = null;
        BlockingQueue<DataPoint> dataQueue = new PriorityBlockingQueue<>(); // get the data out in timestamp order

        try {
            // get a proper InetAddress
            InetAddress multicastGroup = InetAddress.getByName(multicastGroupName);

            // transmitter will send data points from the queue
            UDPtx = new UDPTransmitter(multicastGroup, multicastPort, dataQueue);

            // responder sits on the network and notifies other devices of where to open a TCP socket
            SDR = new ServiceDiscoveryResponder(multicastGroup, multicastPort, serviceName, servicePort, parametersPath);
        } catch (IOException e) {
            e.printStackTrace();
            System.exit(1);
        }

        // create the logger to enqueue data points from sensors
        logger = new DataLogger(sensors, dataQueue);

        // run logger on a thread to allow additional tasks
        Thread loggerThread = new Thread(logger);
        loggerThread.start();

        // give TX a thread to preserve data integrity
        Thread txThread = new Thread(UDPtx);
        txThread.start();

        // open the service responder to support TCP connections
        Thread responderThread = new Thread(SDR);
        responderThread.start();

        // wait for user to quit
        Scanner stdin = new Scanner(System.in);
        while(true) {
            if(stdin.hasNext()) // enter q to quit
                if(Character.toUpperCase(stdin.next().charAt(0)) == 'Q')
                    break;
        }

        // quit
        logger.end();
        UDPtx.end();
        SDR.end();
        try {
            loggerThread.join();
            txThread.join();
            responderThread.join();
        } catch (InterruptedException e) {
            e.printStackTrace();
        }
    }
}

final class ConfigLoader {

    // load sensors given a text file name
    public static Sensor[] getSensorsFromFile(String filename) {
        try {
            return getSensorsFromFile(new FileInputStream(new File(filename)));
        } catch (IOException e) {
            e.printStackTrace();
            System.exit(1);
        }

        return null; // will never be reached
    }

    // load sensors from an input stream
    public static Sensor[] getSensorsFromFile(InputStream fileStream) {
        ArrayList<Sensor> sensors = new ArrayList<>();
        Properties props = new Properties();

        // load configuration file
        try {
            props.load(fileStream);
        } catch (IOException e) {
            e.printStackTrace();
            System.exit(1);
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
                    if(parameters.length != 4) {
                        System.err.println("config.properties error (" + sensorName + "): incorrect number of parameters (Spoof type requires exactly 4)");
                        continue iterateProperties;
                    }
                    sensors.add(new SpoofSensor(sensorName, refreshPeriods));
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

        // return a fixed array; system should be unpowered when changing physical configuration anyway
        return sensors.toArray(new Sensor[0]);
    }
}
