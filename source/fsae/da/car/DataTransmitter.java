package fsae.da.car;

import fsae.da.DataPoint;

import java.util.Queue;

/**
 * Created by brian on 11/15/16.
 * This is where additional forms of output can be implemented
 */
public class DataTransmitter implements Runnable {
    Queue<DataPoint> dataQueue;
    boolean done = false;

    public DataTransmitter(Queue<DataPoint> q, String pitIP, int pitPort, String broadcastIP, int broadcastPort) {
        dataQueue = q;
    }

    @Override
    public void run() {

        while(!(done && dataQueue.isEmpty())) {


        }
    }

    public void end() { done = true; }
}
