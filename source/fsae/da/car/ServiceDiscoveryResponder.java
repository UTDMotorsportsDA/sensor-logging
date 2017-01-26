package fsae.da.car;

import java.io.IOException;
import java.net.DatagramPacket;
import java.net.DatagramSocket;
import java.net.InetAddress;
import java.net.MulticastSocket;
import java.nio.charset.StandardCharsets;
import java.util.Calendar;
import java.util.Date;

// responds to requests on the multicast group with TCP connection information
public class ServiceDiscoveryResponder implements Runnable {
    // response info
    private String serviceName; // what the service is called
    private int servicePort; // the service's port
    private String parametersLocation; // the service's extra info file

    // listening info
    private InetAddress multicastGroup;
    private int multicastPort;

    // signal when to end
    private boolean done = false;

    // set running parameters
    public ServiceDiscoveryResponder(InetAddress multicastGroup, int multicastPort, String serviceName, int serverSocketPort, String parametersLocation) {
        this.multicastGroup = multicastGroup;
        this.multicastPort = multicastPort;
        this.serviceName = serviceName;
        this.servicePort = serverSocketPort;
        this.parametersLocation = parametersLocation;
    }

    private String currentTimeString() {
        Calendar cal = Calendar.getInstance();
        return cal.get(Calendar.HOUR_OF_DAY) + ":" + cal.get(Calendar.MINUTE) +":" + cal.get(Calendar.SECOND) + "." + cal.get(Calendar.MILLISECOND);
    }

    @Override
    public void run() {
        try(DatagramSocket outgoingSocket = new DatagramSocket();
            MulticastSocket listenSocket = new MulticastSocket(multicastPort)) {
            // join the multicast group
            listenSocket.joinGroup(multicastGroup);
            // hold a packet to receive messages from the group
            DatagramPacket rcvPkt = new DatagramPacket(new byte[512], 512);

            // prepare a response for requestors
            String responseString = "<?xml version=\"1.0\" encoding=\"UTF-8\"?>\n" +
                    "<discovery type=\"response\">" +
                    "<name>" + serviceName + "</name>" +
                    "<location>" + listenSocket.getLocalAddress().getHostName() + ":" + servicePort + "</location>" +
                    "<params>" + parametersLocation + "</params>" +
                    "<clock>";

            // debug
            System.out.println("prepared response:\n" + responseString + currentTimeString() + "</clock></discovery>");

            while(!done) {
                // get a message
                listenSocket.receive(rcvPkt);

                // dump the message to console
                System.out.println(new String(rcvPkt.getData(), 0, rcvPkt.getLength(), StandardCharsets.US_ASCII));
            }
        } catch (IOException e) {
            e.printStackTrace();
            System.exit(1);
        }
    }

    // stop the thread
    public void end() { done = true; }
}
