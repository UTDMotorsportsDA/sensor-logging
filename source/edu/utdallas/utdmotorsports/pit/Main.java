package edu.utdallas.utdmotorsports.pit;

/**
 * This is the Main class for pit software - instantiate classes and call methods here.
 * Changes to this file are not saved in the GitHub repository - it is recommended you create separate test classes.
 */
public class Main {
    public static void main(String[] args) {
//        // load configuration file
//        Properties props = new Properties();
//        try {
//            props.load(new FileInputStream(new File("config/general.prop")));
//        } catch (IOException e) {
//            e.printStackTrace();
//            System.exit(1);
//        }
//
//        String multicastGroupName = props.getProperty("multicast_group");
//        if(multicastGroupName == null) {
//            System.err.println("could not find multicast_group in " + args[0]);
//            System.exit(1);
//        }
//        String mcastPort = props.getProperty("multicast_port");
//        if(mcastPort == null) {
//            System.err.println("could not find multicast_port in " + args[0]);
//            System.exit(1);
//        }
//        int multicastPort = Integer.parseInt(mcastPort);
//
//        // sanity check
//        System.out.println("Multicast Address: " + multicastGroupName + ":" + multicastPort);
//
//        MulticastListener listener = null;
//        try {
//            listener = new MulticastListener(InetAddress.getByName(multicastGroupName), multicastPort);
//        } catch(IOException e) {
//            e.printStackTrace();
//            System.exit(1);
//        }
//
//        // start on separate thread to allow additional work
//        new Thread(listener).start();
//
//        // quit when user is done
//        Scanner stdin = new Scanner(System.in);
//        while(!stdin.next().toUpperCase().equals("Q"));
//            listener.quit();
//
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
