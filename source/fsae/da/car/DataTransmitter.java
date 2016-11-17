package fsae.da.car;

import fsae.da.DataPoint;

import java.io.IOException;
import java.io.OutputStream;
import java.io.PrintWriter;
import java.net.*;
import java.nio.charset.StandardCharsets;
import java.util.concurrent.BlockingQueue;
import java.util.concurrent.TimeUnit;

/**
 * Created by brian on 11/15/16.
 * This is where additional forms of output can be implemented
 */
public class DataTransmitter implements Runnable {
    BlockingQueue<DataPoint> dataQueue;
    OutputStream[] outStreams;
    boolean done = false;
    InetAddress broadcastAddress;
    int broadcastPort;

    public DataTransmitter(BlockingQueue<DataPoint> q, OutputStream[] streams, String broadcastIP, int broadcastPort) {
        dataQueue = q;
        outStreams = streams;
        this.broadcastPort = broadcastPort;
        try {
            this.broadcastAddress = InetAddress.getByName(broadcastIP);
        } catch (UnknownHostException e) {
            e.printStackTrace();
        }
    }

    @Override
    public void run() {
        PrintWriter[] outWriters = new PrintWriter[outStreams.length];
        for(int i = 0; i < outWriters.length; ++i) {
            outWriters[i] = new PrintWriter(outStreams[i], true);
        }

        try(DatagramSocket bcastSocket = new DatagramSocket()) {
            System.out.println("Client is up");
            DataPoint currentPoint = null;
            while (!(done && dataQueue.isEmpty())) {
                try {
                    currentPoint = dataQueue.poll(1, TimeUnit.SECONDS);
                } catch (InterruptedException e) {
                    e.printStackTrace();
                }

                // poll again if nothing found in the queue
                if (currentPoint == null)
                    continue;

                // broadcast via UDP
                byte[] data = currentPoint.toString().getBytes(StandardCharsets.US_ASCII);
                bcastSocket.send(
                        new DatagramPacket(data, data.length, broadcastAddress, broadcastPort)
                );

                // send over streams (including socket)
                for(PrintWriter w : outWriters)
                    w.println(currentPoint);
            }
        } catch (SocketException e) {
            e.printStackTrace();
        } catch (IOException e) {
            e.printStackTrace();
        }
    }

    public void end() { done = true; }
}
