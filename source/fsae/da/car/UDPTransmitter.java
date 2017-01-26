package fsae.da.car;

import fsae.da.DataPoint;

import java.io.IOException;
import java.net.DatagramPacket;
import java.net.InetAddress;
import java.net.MulticastSocket;
import java.nio.charset.StandardCharsets;
import java.util.concurrent.BlockingQueue;
import java.util.concurrent.TimeUnit;

/**
 * specifically intended to broadcast data points from the queue over UDP
 */
public final class UDPTransmitter implements Runnable {
    // data transmission parameters
    private InetAddress multicastAddress;
    private int multicastPort;
    private BlockingQueue<DataPoint> dataQueue;

    // signal end of program
    private boolean done = false;

    // save parameters for when a thread is started
    public UDPTransmitter(InetAddress multicastAddress, int multicastPort, BlockingQueue<DataPoint> dataQueue) {
        this.multicastAddress = multicastAddress;
        this.multicastPort = multicastPort;
        this.dataQueue = dataQueue;
    }

    @Override
    public void run() {
        // open a UDP socket
        try(MulticastSocket multicastSocket = new MulticastSocket()) {
            // work with one point at a time
            DataPoint currentPoint = null;

            // main wait-transmit loop
            // once end() is called, flush the data queue and finish
            while(!(done && dataQueue.isEmpty())) {
                // get one data point from the queue
                try {
                    currentPoint = dataQueue.poll(1, TimeUnit.SECONDS); // wait 1 second at most
                } catch (InterruptedException e) {
                    e.printStackTrace(); // no known reason for interruption
                }
                // go again if there's no data to work on
                if(currentPoint == null) continue;

                // get the data point in bytes
                // send bytes over UDP
                byte[] rawBytes = (currentPoint.toString() + '\n').getBytes(StandardCharsets.US_ASCII);
                multicastSocket.send(new DatagramPacket(rawBytes, rawBytes.length, multicastAddress, multicastPort));
            }
        } catch (IOException e) {
            e.printStackTrace();
            System.exit(1);
        }
    }

    // tell the running thread it's done
    public void end() { done = true; }
}
