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

//        // grab relevant parameters
//        String broadcast_IP = props.getProperty("broadcast_IP");
//        if(broadcast_IP == null) {
//            System.err.println("could not find broadcast_IP in " + args[0]);
//            System.exit(1);
//        }
//        if(props.getProperty("broadcast_port") == null) {
//            System.err.println("could not find broadcast_port in " + args[0]);
//            System.exit(1);
//        }
//        int broadcast_port = Integer.parseInt(props.getProperty("broadcast_port")); // more useful as an int

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

        // sanity check
        System.out.println("Multicast Address: " + multicastGroupName + ":" + multicastPort);
        System.out.println("Config Filepath: " + args[1]);

        System.exit(0);

        // load sensors
        Sensor[] sensors = ConfigLoader.getSensorsFromFile(args[1]);

        // client to collect and enqueue data, transmitter to broadcast from the queue
        DataLogger logger = null;
        UDPTransmitter UDPtx = null;
        BlockingQueue<DataPoint> dataQueue = new PriorityBlockingQueue<>(); // get the data out in timestamp order

        try {
            // get a proper InetAddress
            InetAddress multicastAddress = InetAddress.getByName(multicastGroupName);

            // transmitter will send data points from the queue
            UDPtx = new UDPTransmitter(multicastAddress, multicastPort, dataQueue);
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
        try {
            loggerThread.join();
            txThread.join();
        } catch (InterruptedException e) {
            e.printStackTrace();
        }
    }
}
