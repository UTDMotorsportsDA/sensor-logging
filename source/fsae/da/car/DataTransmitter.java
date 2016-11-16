package fsae.da.car;

import fsae.da.DataPoint;

import java.io.BufferedOutputStream;
import java.io.OutputStream;
import java.net.DatagramSocket;
import java.util.ArrayList;
import java.util.Queue;

/**
 * Created by brian on 11/15/16.
 * This is where additional forms of output can be implemented
 */
public class DataTransmitter implements Runnable {
    Queue<DataPoint> dataQueue;
    boolean done = false;

    public DataTransmitter(Queue<DataPoint> q, OutputStream[] streams, DatagramSocket broadcastSocket, String broadcastIP) {
        dataQueue = q;
    }

    @Override
    public void run() {

        while(!(done && dataQueue.isEmpty())) {
            break;

        }
    }

    public void end() { done = true; }
}
