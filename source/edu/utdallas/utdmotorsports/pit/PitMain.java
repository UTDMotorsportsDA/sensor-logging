package edu.utdallas.utdmotorsports.pit;

import java.io.File;
import java.io.FileInputStream;
import java.io.IOException;
import java.net.DatagramPacket;
import java.net.DatagramSocket;
import java.net.InetAddress;
import java.nio.charset.StandardCharsets;
import java.util.Properties;
import java.util.Scanner;

/* Classes in this package are used purely to aid in the development of the .car package.
 * The .pit package may be used as an example, but no guarantees toward quality are made.
 */
public class PitMain {
    public static void main(String[] args) {
        // load configuration file
        Properties props = new Properties();
        try {
            props.load(new FileInputStream(new File("config/general.prop")));
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

        // sanity check
        System.out.println("Multicast Address: " + multicastGroupName + ":" + multicastPort);

        MulticastListener listener = null;
        try {
            listener = new MulticastListener(InetAddress.getByName(multicastGroupName), multicastPort);
        } catch(IOException e) {
            e.printStackTrace();
            System.exit(1);
        }

        // start on separate thread to allow additional work
        new Thread(listener).start();

        // quit when user is done
        Scanner stdin = new Scanner(System.in);
        while(!stdin.next().toUpperCase().equals("Q"));
            listener.quit();

//        // wait for some user input
//        new Scanner(System.in).next();
//
//        // one shot
//        try(DatagramSocket skt = new DatagramSocket()) {
//            skt.setReuseAddress(true);
//            String msg = "{\"discovery request\":{\"name\":\"" + props.getProperty("service_name") + "\"}}";
//            byte[] bytes = msg.getBytes(StandardCharsets.US_ASCII);
//            skt.send(new DatagramPacket(bytes, bytes.length, InetAddress.getByName(multicastGroupName), multicastPort));
//        } catch (IOException e) {
//            e.printStackTrace();
//        }
    }
}
