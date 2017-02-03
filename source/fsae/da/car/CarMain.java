package fsae.da.car;

import fsae.da.DataPoint;

import java.io.File;
import java.io.FileInputStream;
import java.io.IOException;
import java.net.InetAddress;
import java.util.Properties;
import java.util.Scanner;
import java.util.concurrent.BlockingQueue;
import java.util.concurrent.PriorityBlockingQueue;

public class CarMain {
    // args: parameter config file, sensor config file
    public static void main(String[] args) {
        // load configuration file
        Properties props = new Properties();
        try {
            props.load(new FileInputStream(new File(args[0])));
        } catch (IOException e) {
            e.printStackTrace();
            System.exit(1);
        }

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

        // sanity check
        System.out.println("Multicast Address: " + multicastGroupName + ":" + multicastPort);
        System.out.println("Config Filepath: " + args[1]);

        // load sensors
        Sensor[] sensors = ConfigLoader.getSensorsFromFile(args[1]);

        // client to collect and enqueue data, transmitter to broadcast from the queue
        DataLogger logger = null;
        UDPTransmitter UDPtx = null;
        ServiceDiscoveryResponder SDR = null;
        BlockingQueue<DataPoint> dataQueue = new PriorityBlockingQueue<>(); // get the data out in timestamp order

        try {
            // get a proper InetAddress
            InetAddress multicastAddress = InetAddress.getByName(multicastGroupName);

            // transmitter will send data points from the queue
            UDPtx = new UDPTransmitter(multicastAddress, multicastPort, dataQueue);

            // responder sits on the network and notifies other devices of where to open a TCP socket
            SDR = new ServiceDiscoveryResponder(multicastAddress, multicastPort, serviceName, servicePort, parametersPath);
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
