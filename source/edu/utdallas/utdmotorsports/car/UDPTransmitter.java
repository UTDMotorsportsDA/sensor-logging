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
 * instances are meant to be added to a QueueMultiProducer
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
}