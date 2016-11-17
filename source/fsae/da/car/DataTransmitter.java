package fsae.da.car;

import fsae.da.DataPoint;

import java.io.BufferedOutputStream;
import java.io.OutputStream;
import java.net.DatagramSocket;
import java.net.SocketException;
import java.util.ArrayList;
import java.util.Queue;

/**
 * Created by brian on 11/15/16.
 * This is where additional forms of output can be implemented
 */
public class DataTransmitter implements Runnable {
    Queue<DataPoint> dataQueue;
    boolean done = false;

    public DataTransmitter(Queue<DataPoint> q, OutputStream[] streams, String broadcastIP, int broadcastPort) {
        dataQueue = q;
    }

    @Override
    public void run() {
        try(DatagramSocket bcastSocket = new DatagramSocket()) {
            System.out.println("Client is up");
            while (!(done && dataQueue.isEmpty())) {
                break;

            }
        } catch (SocketException e) {
            e.printStackTrace();
        }
    }

    public void end() { done = true; }
}
