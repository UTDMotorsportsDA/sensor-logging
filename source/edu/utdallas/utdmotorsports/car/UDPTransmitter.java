package edu.utdallas.utdmotorsports.car;

import edu.utdallas.utdmotorsports.DataPoint;
import edu.utdallas.utdmotorsports.QueueMultiConsumer;

import java.io.IOException;
import java.net.DatagramPacket;
import java.net.DatagramSocket;
import java.net.InetAddress;
import java.nio.charset.StandardCharsets;

/**
 * specifically intended to multicast data points from the queue to UDP group
 */
public final class UDPTransmitter implements QueueMultiConsumer<DataPoint> {
    // data transmission parameters
    private InetAddress multicastAddress;
    private int multicastPort;


    // save parameters for when a thread is started
    public UDPTransmitter(InetAddress multicastAddress, int multicastPort) {
        this.multicastAddress = multicastAddress;
        this.multicastPort = multicastPort;
    }

    @Override
    public void processElement(DataPoint dataPoint) {
        // make a one-time-use UDP socket
        try (DatagramSocket multicastSocket = new DatagramSocket()) {
            // encode data in JSON and get char array
            // send over UDP
            byte[] rawBytes = ("{\"data\":\"" + dataPoint + "\"}").getBytes(StandardCharsets.US_ASCII);
            multicastSocket.send(new DatagramPacket(rawBytes, rawBytes.length, multicastAddress, multicastPort));
        } catch (IOException e) {
            e.printStackTrace();
        }
    }

//    @Override
//    public void run() {
//        // open a UDP socket
//        try(DatagramSocket multicastSocket = new DatagramSocket()) {
//            // work with one point at a time
//            DataPoint currentPoint = null;
//
//            // main wait-transmit loop
//            // once end() is called, flush the data queue and finish
//            while(!(done && dataQueue.isEmpty())) {
//                // get one data point from the queue
//                try {
//                    currentPoint = dataQueue.poll(1, TimeUnit.SECONDS); // wait 1 second at most
//                } catch (InterruptedException e) {
//                    e.printStackTrace(); // no known reason for interruption
//                }
//                // go again if there's no data to work on
//                if(currentPoint == null) continue;
//
//
//            }
//        } catch (IOException e) {
//            e.printStackTrace();
//            System.exit(1);
//        }
//    }
}
