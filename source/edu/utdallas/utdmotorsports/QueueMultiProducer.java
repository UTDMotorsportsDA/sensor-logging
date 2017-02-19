package edu.utdallas.utdmotorsports;

import java.util.*;
import java.util.concurrent.BlockingQueue;
import java.util.concurrent.TimeUnit;

// consume data from the input queue and distribute it to consumers
public class QueueMultiProducer<E> implements Runnable, Stoppable {
    // objects that need queue data
    // queue to supply data
    private Set<QueueMultiConsumer<E>> consumers;
    private BlockingQueue<E> inputQueue;

    // track whether or not it's time to exit
    // store this instance's thread so it can be interrupted
    private volatile boolean done = false;
    private Thread thisThread;

    // must use the pausing nature of BlockingQueue
    public QueueMultiProducer(BlockingQueue<E> inputQueue) {
        // create the set that will store consumers
        consumers = new HashSet<>();

        // hold onto the queue
        this.inputQueue = inputQueue;
    }

    // add a consumer to the list
    // return false if consumer is already in the list (handled by Java.util.Set)
    public boolean addConsumer(QueueMultiConsumer<E> newConsumer) {
        return consumers.add(newConsumer);
    }

    // repeatedly poll the queue and distribute
    @Override
    public void run() {
        // grab the running thread
        thisThread = Thread.currentThread();

        try {
            while (!done) {
                // retrieve an element from the queue, don't let null pass
                E element;
                System.out.println("PRODUCER WAITING");
                while(null == (element = inputQueue.poll(10, TimeUnit.SECONDS)));
                System.out.println("PRODUCER RUNNING");

                // distribute sequentially to all consumers
                for(QueueMultiConsumer<E> consumer : consumers)
                    consumer.processElement(element);
            }
        } catch (InterruptedException e) {
            if (!done)
                e.printStackTrace();
            else
                System.out.println("interrupted");
        }
    }

    @Override
    public void quit() {
        done = true;

        // break o
        thisThread.interrupt();
    }
}
