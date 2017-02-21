package edu.utdallas.utdmotorsports.car;

import edu.utdallas.utdmotorsports.DataPoint;
import edu.utdallas.utdmotorsports.QueueMultiProducer;
import edu.utdallas.utdmotorsports.car.sensors.*;

import java.io.File;
import java.io.FileInputStream;
import java.io.IOException;
import java.io.InputStream;
import java.net.InetAddress;
import java.time.Duration;
import java.util.ArrayList;
import java.util.Enumeration;
import java.util.Properties;
import java.util.Scanner;
import java.util.concurrent.BlockingQueue;
import java.util.concurrent.PriorityBlockingQueue;

public class CarMain {
    private static String DEFAULT_CONFIG_FILE = "config/general.prop";
    private static String DEFAULT_SENSORS_FILE = "config/sensor.prop";

    // configurable parameters
    private static String multicastGroupName;
    private static int multicastPort;
    private static String serviceName;
    private static String parametersPath;
    private static int servicePort;
    private static int serviceMaxConnectionCount;
    private static ArrayList<Sensor> sensors;

    private static void loadConfigs(String[] args) {
        // these contain final selection of config
        String chosenConfigFile, chosenSensorsFile;

        // select config files (allow for specifying)
        if(args.length >= 2) {
            chosenConfigFile = args[0];
            chosenSensorsFile = args[1];
        }
        else {
            chosenConfigFile = DEFAULT_CONFIG_FILE;
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
        multicastGroupName = props.getProperty("multicast_group");
        if(multicastGroupName == null) {
            System.err.println("could not find multicast_group in " + chosenConfigFile);
            System.exit(1);
        }
        String mcastPort = props.getProperty("multicast_port");
        if(mcastPort == null) {
            System.err.println("could not find multicast_port in " + chosenConfigFile);
            System.exit(1);
        }
        multicastPort = Integer.parseInt(mcastPort);

        serviceName = props.getProperty("service_name");
        if(serviceName == null) {
            System.err.println("could not find service_name in " + chosenConfigFile);
            System.exit(1);
        }
        parametersPath = props.getProperty("parameters_location");
        if(parametersPath == null) {
            System.err.println("could not find parameters_location in " + chosenConfigFile);
            System.exit(1);
        }
        String svcPort = props.getProperty("server_socket_port");
        if(svcPort == null) {
            System.err.println("could not find server_socket_port in " + chosenConfigFile);
            System.exit(1);
        }
        servicePort = Integer.parseInt(svcPort);
        String svcMaxConn = props.getProperty("max_connection_count");
        if(svcMaxConn == null) {
            System.err.println("could not find max_connection_count in " + chosenConfigFile);
            System.exit(1);
        }
        serviceMaxConnectionCount = Integer.parseInt(svcMaxConn);

        // load sensors
        if(chosenSensorsFile.charAt(0) == '/' || chosenSensorsFile.substring(0, 2).equals("./"))
            // get the sensors file from the user's filesystem
            sensors = ConfigLoader.getSensorsFromFile(chosenSensorsFile);
        else
            // get the sensors file saved in this JAR
            sensors = ConfigLoader.getSensorsFromFile(CarMain.class.getResourceAsStream("/" + chosenSensorsFile));
    }

    // args: parameter config file, sensor config file
    public static void main(String[] args) {
        loadConfigs(args);

        // sanity check
        System.out.println("Multicast Group: " + multicastGroupName + ":" + multicastPort);

        // client to collect and enqueue data, transmitter to broadcast from the queue
        DataLogger logger = null;
        ServiceDiscoveryResponder SDR = null;
        BlockingQueue<DataPoint> dataQueue = new PriorityBlockingQueue<>(); // get the data out in timestamp order

        // create the logger to enqueue data points from sensors
        logger = new DataLogger(sensors, dataQueue);

        // run logger on a thread to allow additional tasks
        Thread loggerThread = new Thread(logger);
        loggerThread.start();

        InetAddress multicastGroup = null;
        try {
            // get a proper InetAddress
            multicastGroup = InetAddress.getByName(multicastGroupName);

            // responder sits on the network and notifies other devices of where to open a TCP socket
            SDR = new ServiceDiscoveryResponder(multicastGroup, multicastPort, serviceName, servicePort, parametersPath);
        } catch (IOException e) {
            e.printStackTrace();
            System.exit(1);
        }

        // manage multiple-consumption of data
        QueueMultiProducer<DataPoint> dataPointQueueManager = new QueueMultiProducer<>(dataQueue);
        dataPointQueueManager.addConsumer(new UDPTransmitter(multicastGroup, multicastPort));

        // start data-managing thread
        Thread dataPointQueueManagerThread = new Thread(dataPointQueueManager);
        dataPointQueueManagerThread.start();

        // open the TCP service on a thread to accept and feed connections
        TCPDataService dataService = new TCPDataService(servicePort, serviceMaxConnectionCount);
        Thread dataServiceThread = new Thread(dataService);
        dataServiceThread.start();

        // data service needs to consume data points
        dataPointQueueManager.addConsumer(dataService);

        // open the service responder to support service discovery
        Thread responderThread = new Thread(SDR);
        responderThread.start();

        // quit when user is done
        Scanner stdin = new Scanner(System.in);
        while(!stdin.next().toUpperCase().equals("Q"));

        // quit all threaded objects
        logger.quit();
        dataPointQueueManager.quit();
        SDR.quit();
        dataService.quit();
        try {
            // join with threads
            loggerThread.join();
            dataPointQueueManagerThread.join();
            responderThread.join();
            dataServiceThread.join();
        } catch (InterruptedException e) {
            e.printStackTrace();
        }
    }
}

final class ConfigLoader {

    // load sensors given a text file name
    public static ArrayList<Sensor> getSensorsFromFile(String filename) {
        try {
            return getSensorsFromFile(new FileInputStream(new File(filename)));
        } catch (IOException e) {
            e.printStackTrace();
            System.exit(1);
        }

        return null; // will never be reached
    }

    // load sensors from an input stream
    public static ArrayList<Sensor> getSensorsFromFile(InputStream fileStream) {
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

                case "SimInt":
                    if(parameters.length != 5) {
                        System.err.println("config.properties error (" + sensorName + "): incorrect number of parameters (SimInt type requires exactly 5)");
                        continue iterateProperties;
                    }
                    try {
                        sensors.add(new SimulatedSensorInt(sensorName, refreshPeriods, Integer.parseInt(parameters[4])));
                    } catch(NumberFormatException ex) {
                        System.err.println("config.properties error (" + sensorName + "): incorrect formatting");
                        continue iterateProperties;
                    }
                    break;

                case "SimFloat":
                    if(parameters.length != 5) {
                        System.err.println("config.properties error (" + sensorName + "): incorrect number of parameters (SimFloat type requires exactly 5)");
                        continue iterateProperties;
                    }
                    try {
                        sensors.add(new SimulatedSensorFloat(sensorName, refreshPeriods, Float.parseFloat(parameters[4])));
                    } catch(NumberFormatException ex) {
                        System.err.println("config.properties error (" + sensorName + "): incorrect formatting");
                        continue iterateProperties;
                    }
                    break;

                case "SimVecFloat":
                    if(parameters.length != 7) {
                        System.err.println("config.properties error (" + sensorName + "): incorrect number of parameters (SimFloat type requires exactly 7)");
                        continue iterateProperties;
                    }
                    try {
                        float[] seed = new float[3];
                        for(int i = 0; i < seed.length; ++i) {
                            seed[i] = Float.parseFloat(parameters[i + 4]);
                        }
                        sensors.add(new SimulatedSensorVecFloat(sensorName, refreshPeriods, seed));
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

        return sensors;
    }
}
