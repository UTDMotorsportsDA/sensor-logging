package edu.utdallas.utdmotorsports.pit;

import edu.utdallas.utdmotorsports.Stoppable;

import java.io.IOException;
import java.net.DatagramPacket;
import java.net.InetAddress;
import java.net.MulticastSocket;
import java.nio.charset.StandardCharsets;

/* Classes in this package are used purely to aid in the development of the .car package.
 * The .pit package may be used as an example, but no guarantees toward quality are made.
 */
// listen and dump all messages on multicast group
public class MulticastListener implements Runnable, Stoppable {

    private InetAddress multicastGroup;
    private int multicastPort;
    private boolean done = false;

    public MulticastListener(InetAddress multicastGroup, int multicastPort) {
        this.multicastGroup = multicastGroup;
        this.multicastPort = multicastPort;
    }

    @Override
    public void run() {
        try(MulticastSocket listenSock = new MulticastSocket(multicastPort)) {
            listenSock.joinGroup(multicastGroup);
            DatagramPacket pkt = new DatagramPacket(new byte[512], 512);

            while(!done) {
                listenSock.receive(pkt);
                System.out.println(new String(pkt.getData(), 0, pkt.getLength(), StandardCharsets.US_ASCII));
            }
        } catch(IOException e) {
            e.printStackTrace();
            System.exit(1);
        }
    }

    @Override
    public void quit() { done = true; }
}
