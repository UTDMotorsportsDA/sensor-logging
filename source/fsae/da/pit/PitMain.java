package fsae.da.pit;

import java.io.File;
import java.io.FileInputStream;
import java.io.IOException;
import java.net.InetAddress;
import java.util.Properties;
import java.util.Scanner;

public class PitMain {
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

        // wait for some user input before ending
        new Scanner(System.in).next();

        // quit
        listener.end();
    }
}
