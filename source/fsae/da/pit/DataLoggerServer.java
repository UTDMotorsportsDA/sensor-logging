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

    // completion condition (allow server to quit)
    private boolean done = false;

    // UDP resources
    private MulticastSocket broadcastReceiveSocket = null;

    public DataLoggerServer(int broadcastReceivePort) {
        this.broadcastReceivePort = broadcastReceivePort;
    }

    @Override
    public void run() {
        try {
            // create resources for receiving broadcast packets
            // note: every single UDP packet received by the machine can come here
            broadcastReceiveSocket = new MulticastSocket(broadcastReceivePort);
            DatagramPacket pkt = new DatagramPacket(new byte[1024], 1024);

            System.out.println("Server is up");

            DataPoint udpData;
            while(!done) {
                // wait to get a packet from the broadcast group
                broadcastReceiveSocket.receive(pkt);

                // convert packet back into a string
                udpData = new DataPoint(new String(pkt.getData(), 0, pkt.getLength(), StandardCharsets.US_ASCII));

                // dump data to the console
                System.out.println(udpData);
            }

        } catch (IOException e) {
            e.printStackTrace();
        }
    }

      public void end() { done = true; broadcastReceiveSocket.close(); }
}
