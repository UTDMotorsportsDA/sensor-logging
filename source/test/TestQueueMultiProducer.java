package test;

import edu.utdallas.utdmotorsports.DataPoint;
import edu.utdallas.utdmotorsports.QueueMultiConsumer;
import edu.utdallas.utdmotorsports.QueueMultiProducer;

import java.time.Instant;
import java.util.ArrayList;
import java.util.List;
import java.util.concurrent.ArrayBlockingQueue;
import java.util.concurrent.BlockingQueue;

/**
 * Created by brian on 2/18/17.
 */
class QueuePrinter implements QueueMultiConsumer<DataPoint> {

    private static int sequentialId = 0;
    private int id;

    public QueuePrinter() {
        id = sequentialId++;
    }

    @Override
    public void processElement(DataPoint dataPoint) {
        System.out.println(Integer.toString(id) + ": " + dataPoint);
    }
}

public class TestQueueMultiProducer {
    public static void main(String[] args) throws InterruptedException{
        BlockingQueue<DataPoint> testQueue = new ArrayBlockingQueue<>(64);
        QueueMultiProducer<DataPoint> testProd = new QueueMultiProducer<>(testQueue);
        for(int i = 0; i < 8; ++i) {
            testProd.addConsumer(new QueuePrinter());
        }

        Thread prodThread = new Thread(testProd);
        prodThread.start();

        for(int i = 0; i < 10; ++i) {
            Thread.sleep(3000);

            testQueue.add(new DataPoint("sensor", "value " + i, Instant.now().toEpochMilli(), true));
        }
        System.out.println("done");
        Thread.sleep(3000);
        testProd.quit();
        prodThread.join();
    }
}
