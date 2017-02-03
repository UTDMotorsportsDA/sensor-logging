package fsae.da.car;

import fsae.da.DataPoint;
import org.json.simple.JSONObject;

import java.io.IOException;
import java.net.DatagramPacket;
import java.net.DatagramSocket;
import java.net.InetAddress;
import java.nio.charset.StandardCharsets;
import java.util.concurrent.BlockingQueue;
import java.util.concurrent.TimeUnit;

/**
 * specifically intended to multicast data points from the queue to UDP group
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
        try(DatagramSocket multicastSocket = new DatagramSocket()) {
            JSONObject jObj = new JSONObject();
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

                // encode data in JSON
                // get the JSON char array
                // send over UDP
                jObj.put("data", currentPoint.toString());
                byte[] rawBytes = (jObj.toJSONString()).getBytes(StandardCharsets.US_ASCII);
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