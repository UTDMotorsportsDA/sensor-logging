package fsae.da.pit;

import fsae.da.DataPoint;

import java.io.BufferedInputStream;
import java.io.IOException;
import java.io.InputStream;
import java.net.DatagramPacket;
import java.net.MulticastSocket;
import java.net.ServerSocket;
import java.net.Socket;
import java.nio.charset.StandardCharsets;
import java.util.Scanner;

public class DataLoggerServer implements Runnable {

    // desired port
    private int broadcastReceivePort = 0;
    private int tcpReceivePort = 0;

    // completion condition (allow server to quit)
    private boolean done = false;

    // UDP resources
    private MulticastSocket broadcastReceiveSocket = null;

    public DataLoggerServer(int broadcastReceivePort, int tcpReceivePort) {
        this.broadcastReceivePort = broadcastReceivePort;
        this.tcpReceivePort = tcpReceivePort;
    }

    @Override
    public void run() {
        try {
            // create resources for receiving broadcast packets
            // note: every single UDP packet received by the machine can come here
            broadcastReceiveSocket = new MulticastSocket(broadcastReceivePort);
            DatagramPacket pkt = new DatagramPacket(new byte[1024], 1024);
            ServerSocket ssock = new ServerSocket(tcpReceivePort);
            Socket tcpReceiveSocket = ssock.accept();
            InputStream tcpInputStream;
            Scanner tcpInput = new Scanner(tcpInputStream = new BufferedInputStream(tcpReceiveSocket.getInputStream()));

            System.out.println("Server is up");

            DataPoint udpData, tcpData;
            while(!done) {
                // wait to get a packet from the broadcast group
                broadcastReceiveSocket.receive(pkt);

                // convert packet back into a string
                udpData = new DataPoint(new String(pkt.getData(), 0, pkt.getLength(), StandardCharsets.US_ASCII));

                // if no data is available over TCP, skip it
                if(tcpInputStream.available() > 0) tcpData = new DataPoint(tcpInput.nextLine());
                else tcpData = new DataPoint("x", "x", 0);

                // dump data to the console
                System.out.println(String.format("%1$-60s", "udp: " + udpData) + " tcp: " + tcpData);
            }

        } catch (IOException e) {
            e.printStackTrace();
        }
    }

      public void end() { done = true; broadcastReceiveSocket.close(); }
}
