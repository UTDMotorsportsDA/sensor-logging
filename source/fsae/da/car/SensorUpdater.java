package fsae.da.car;

import java.time.Duration;
import java.time.Instant;
import java.util.PriorityQueue;
import java.util.Queue;

// this class is intended to operate any sensor without optimization from specific sensor details
// either use this or inherit from it to optimize updating
public class SensorUpdater implements Runnable {

    protected DataLoggerClient ownerLogger = null;
    protected Queue<ComparableSensor> sensorQueue = new PriorityQueue<>();
    protected boolean done = false;

    // used to wait on an empty sensor queue
    private final Object monitor = new Object();

    @Override
    public void run() {
        while(!done) {
            // if this instance has no sensors in it, sit here and wait
            synchronized(monitor) {
                while(sensorQueue.size() < 1) {// .wait is prone to spurious wakeups, hold here if size < 1
                    try { monitor.wait(); } catch (InterruptedException e) { }
                }
            }

            // retrieve next sensor from queue
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
                    e.printStackTrace();
                }
            }

            // update sensor
            if(currentSensor.refresh()) {
                // sensor's critical state has changed, renew in client logger
                ownerLogger.renewSensor(currentSensor);
            }

            // re-enqueue (updater's queue is never subject to
            // noticeable outside interference)
            sensorQueue.add(currentComparableSensor);
        }
    }

    public SensorUpdater(DataLoggerClient logger) {
        this.ownerLogger = logger;
    }
    public SensorUpdater(DataLoggerClient logger, Sensor[] sensors) {
        for(Sensor s : sensors)
            addSensor(s);
    }

    // returns true if sensor is accepted and added, false otherwise
    public boolean addSensor(Sensor s) {
        sensorQueue.add(s.asComparable(RefreshType.VALUE_UPDATE));
        synchronized(monitor) { monitor.notify(); } // the update thread can wake up and check the queue
        return true;
    }

    public void end() { done = true; }
}
