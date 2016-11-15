package fsae.da.car;

import fsae.da.DataPoint;

import java.io.IOException;
import java.net.DatagramPacket;
import java.net.DatagramSocket;
import java.net.InetAddress;
import java.net.UnknownHostException;
import java.nio.charset.StandardCharsets;
import java.time.Duration;
import java.time.Instant;
import java.util.PriorityQueue;
import java.util.Queue;
import java.util.concurrent.PriorityBlockingQueue;

public class DataLoggerClient implements Runnable {

    private InetAddress broadcastAddress = null;
    private int port = 0;
    private Queue<ComparableSensor> sensorQueue = new PriorityQueue<>();
    private Queue<DataPoint> outputQueue;
    boolean done = false;
    private static final RefreshType DLC_REFRESH_TYPE = RefreshType.PIT;
    private Thread clientThread = null;

    public DataLoggerClient(String broadcastIP, int commPort, Sensor[] sensors, Queue<DataPoint> outputQueue) throws UnknownHostException {
        this.broadcastAddress = InetAddress.getByName(broadcastIP);
        port = commPort;
        this.outputQueue = outputQueue;

        // wrap sensors in objects that implement
        // Comparable for the priority queue
        for(Sensor s : sensors) {
            sensorQueue.add(s.asComparable(DLC_REFRESH_TYPE));
        }
    }

    // intended to inform this instance of a new update interval immediately
    // removes sensor if possible and adds; different from requeueComparableSensor
    public synchronized void renewSensor(Sensor s) {
        // if sensor is in queue, remove
        sensorQueue.remove(s.asComparable(DLC_REFRESH_TYPE));

        // add sensor to queue (if sensor was in queue, moves it to the new update period)
        sensorQueue.add(s.asComparable(DLC_REFRESH_TYPE));

        // kick the logger out of its current wait period to poll for next sensor
        // if a sensor has gone critical, it needs to be handled ASAP
        clientThread.interrupt();
    }

    // part of run()'s queue'd cycle put into a method for thread synchronization
    private synchronized void requeueComparableSensor(ComparableSensor cs) {
        // only re-add sensor if it wasn't added by a call to renewSensor
        if(!sensorQueue.contains(cs))
            sensorQueue.add(cs);
    }

    @Override
    public void run() {
        // grab a handle to this thread
        clientThread = Thread.currentThread();

        // create specific updaters, make the first updater a catch-all
        SensorUpdater[] updaters = { new SensorUpdater(this),
                new SpoofSensorUpdater(this) };

        // add every sensor from inside wrapper to the updaters
        // if sensor can't be placed in any specific updater, place it in the default
        // if sensor can't even go in the default updater, log as an error and move on
        for(ComparableSensor cs : sensorQueue.toArray(new ComparableSensor[sensorQueue.size()])) {
            int updaterIndex = updaters.length;
            while(updaterIndex > 0 && !updaters[--updaterIndex].addSensor(cs.sensor()));
            if(updaterIndex < 0)
                System.err.println("unable to add sensor \"" + cs.sensor().getLabel() + "\" to any updater");
        }

        // kick off the updaters on separate threads
        Thread[] updaterThreads = new Thread[updaters.length];
        for(int i = 0; i < updaters.length; i++) {
            updaterThreads[i] = new Thread(updaters[i]);
            updaterThreads[i].start();
        }

        // open a socket for broadcasting data
        try(DatagramSocket broadcastSocket = new DatagramSocket()) {

            System.out.println("Client is up.");

            while(!done) {
                // retrieve a sensor from which to read out of the queue
                ComparableSensor currentComparableSensor = sensorQueue.poll();
                Sensor currentSensor = currentComparableSensor.sensor();

                // wait until moment of update
                Duration delta = Duration.between(Instant.now(), currentComparableSensor.nextRefresh());
                if(delta.compareTo(Duration.ZERO) > 0) {
                    long millisToWait = delta.toMillis();
                    int nanosToWait = Math.max(0, (int)delta.minusMillis(millisToWait).toNanos());

                    try {
                        Thread.sleep(millisToWait, nanosToWait);
                    } catch (InterruptedException e) {
                        // skip the rest of waiting for the next sensor reading, some critical state has changed
                    }
                }

                // convert a formatted data point string into an equivalent ascii byte array
                byte[] dataBytes = (currentComparableSensor.sensor().getLabel() + "=" + currentSensor.getCurrent() + "@" + Instant.now().toEpochMilli()).getBytes(StandardCharsets.US_ASCII);
                broadcastSocket.send(
                        new DatagramPacket(dataBytes, dataBytes.length, broadcastAddress, port)
                );
//                outputQueue.add(new DataPoint(currentComparableSensor.sensor().getLabel(), currentComparableSensor.sensor().getCurrent(), Instant.now().toEpochMilli()));

                // re-enqueue sensor for next update
                requeueComparableSensor(currentComparableSensor);
            }
        } catch (IOException e) {
            e.printStackTrace();
        } finally {
            try {
                // temporary: kill the updaters and wait for them to end
                for(SensorUpdater u : updaters)
                    u.end();
                for(Thread t : updaterThreads)
                    t.join();
            } catch (InterruptedException e) {
                e.printStackTrace();
            }
        }

    }

    // allow client to finish
    public void end() { done = true; }
}
