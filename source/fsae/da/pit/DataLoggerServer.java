package fsae.da.pit;

import java.io.IOException;
import java.net.DatagramPacket;
import java.net.MulticastSocket;
import java.nio.charset.StandardCharsets;

public class DataLoggerServer implements Runnable {

    // desired port
    private int port = 0;

    // completion condition (allow server to quit)
    private boolean done = false;

    // UDP resources
    private MulticastSocket receiveSocket = null;

    public DataLoggerServer(int port) {
        this.port = port;
    }

    @Override
    public void run() {
        try {
            // create resources for receiving broadcast packets
            // note: every single UDP packet received by the machine can come here
            receiveSocket = new MulticastSocket(port);
            DatagramPacket pkt = new DatagramPacket(new byte[1024], 1024);

            System.out.println("Server is up");

            while(!done) {
                // wait to get a packet from the broadcast group
                receiveSocket.receive(pkt);

                // convert packet back into a string
                String dataPoint = new String(pkt.getData(), 0, pkt.getLength(), StandardCharsets.US_ASCII);

                // dump data to the console
                System.out.println(dataPoint);
            }

        } catch (IOException e) {
            e.printStackTrace();
        }
    }

      public void end() { done = true; receiveSocket.close(); }
}
