package fsae.da.car;

import java.io.IOException;
import java.net.DatagramPacket;
import java.net.DatagramSocket;
import java.net.InetAddress;
import java.net.UnknownHostException;
import java.nio.charset.StandardCharsets;
import java.time.Duration;
import java.time.Instant;
import java.util.Queue;
import java.util.concurrent.PriorityBlockingQueue;

public class DataLoggerClient implements Runnable {

    private InetAddress broadcastAddress = null;
    private int port = 0;
    private Queue<ComparableSensor> sensorQueue = new PriorityBlockingQueue<ComparableSensor>();
    boolean done = false;
    private static final RefreshType DLC_REFRESH_TYPE = RefreshType.PIT;
    private Thread clientThread = null;

    public DataLoggerClient(String broadcastIP, int commPort, Sensor[] sensors) throws UnknownHostException {
        this.broadcastAddress = InetAddress.getByName(broadcastIP);
        port = commPort;

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

        // start the sensor update thread
        SensorUpdater updater0 = new SpoofSensorUpdater(this);

        // add every sensor from inside wrapper to the updater
        for(ComparableSensor cs : sensorQueue.toArray(new ComparableSensor[sensorQueue.size()]))
            updater0.addSensor(cs.sensor());

        // kick off the updater on a separate thread
        Thread updater0Thread = new Thread(updater0);
        updater0Thread.start();

        // open a socket for broadcasting data
        try(DatagramSocket broadcastSocket = new DatagramSocket()) {

            System.out.println("Client is up.");

            while(!done) {
                // retrieve a sensor from which to read out of the queue
                ComparableSensor currentComparableSensor = sensorQueue.poll();
                Sensor currentSensor = currentComparableSensor.sensor();

                // wait until time to update
                Duration negativeDelta = Duration.between(currentComparableSensor.nextRefresh(), Instant.now());
                if(negativeDelta.isNegative()) {

                    long millis = -1 * negativeDelta.toMillis();
                    int nanos = Math.max(0, -1 * (int)negativeDelta.plusMillis(millis).toNanos());

                    try {
                        Thread.sleep(millis, nanos);
                    } catch (InterruptedException e) {
                        // skip the rest of waiting for the next sensor reading, some critical state has changed
                    }
                }

                // convert a formatted data point string into an equivalent ascii byte array
                byte[] dataBytes = (currentComparableSensor.sensor().getLabel() + "=" + currentSensor.getCurrent()).getBytes(StandardCharsets.US_ASCII);
                broadcastSocket.send(
                        new DatagramPacket(dataBytes, dataBytes.length, broadcastAddress, port)
                );

                // re-enqueue sensor for next update
                requeueComparableSensor(currentComparableSensor);
            }
        } catch (IOException e) {
            e.printStackTrace();
        } finally {
            try {
                // temporary: kill the updater and wait for it to end
                updater0.end();
                updater0Thread.join();
            } catch (InterruptedException e) {
                e.printStackTrace();
            }
        }

    }

    // allow client to finish
    public void end() { done = true; }
}