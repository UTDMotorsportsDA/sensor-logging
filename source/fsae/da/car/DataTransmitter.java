package fsae.da.car;

import fsae.da.DataPoint;

import java.io.IOException;
import java.io.OutputStream;
import java.net.*;
import java.nio.charset.StandardCharsets;
import java.util.List;
import java.util.concurrent.BlockingQueue;
import java.util.concurrent.TimeUnit;

/**
 * Created by brian on 11/15/16.
 * Transmitter will write to UDP sockets and OutputStreams, including:
 *      UDP broadcast ports,
 *      TCP sockets,
 *      files,
 *      etc
 * This is where additional forms of output should be implemented
 */
public class DataTransmitter implements Runnable {
    // queue from which to read data
    private BlockingQueue<DataPoint> dataQueue;

    // non-UDP data destinations
    private List<OutputStream> outStreams;

    // UDP broadcast destination parameters
    private InetAddress broadcastAddress;
    private int broadcastPort;

    // allow stop condition
    private boolean done = false;

    public DataTransmitter(BlockingQueue<DataPoint> q, List<OutputStream> streams, String broadcastIP, int broadcastPort) throws UnknownHostException {
        dataQueue = q;
        outStreams = streams;
        this.broadcastPort = broadcastPort;
        this.broadcastAddress = InetAddress.getByName(broadcastIP);
    }

    @Override
    public void run() {

        try(DatagramSocket bcastSocket = new DatagramSocket()) {
            System.out.println("Client is up");
            DataPoint currentPoint = null; // repeatedly grab data points from the queue
            while (!(done && dataQueue.isEmpty())) { // quit only when told to AND the queue is empty
                try {
                    currentPoint = dataQueue.poll(1, TimeUnit.SECONDS); // time out after 1 second of the queue being empty
                } catch (InterruptedException e) {
                    e.printStackTrace(); // no known reason for interruption
                }

                // poll again if nothing found in the queue
                if (currentPoint == null)
                    continue;

                // broadcast via UDP
                byte[] data = (currentPoint.toString() + "\n").getBytes(StandardCharsets.US_ASCII);
                bcastSocket.send(
                        new DatagramPacket(data, data.length, broadcastAddress, broadcastPort)
                );

                // write to streams
                for(OutputStream s : outStreams) {
                    s.write(data);
                }
            }
        } catch (SocketException e) {
            e.printStackTrace();
        } catch (IOException e) {
            e.printStackTrace();
        }
    }

    public void end() { done = true; }
}
