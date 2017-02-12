package fsae.da;

import java.util.Queue;

// consume data from the input queue and produce that data into multiple output queues
public class QueueSplitter<E> {
    private Queue<E>[] outputQueues;

    public QueueSplitter(int numOutputQueues, Queue<E> inputQueue) throws IllegalArgumentException {
        if(numOutputQueues < 2)
            throw new IllegalArgumentException("splitting into " + numOutputQueues + " queues is invalid");

    }

    // access a particular queue
    public Queue<E> getQueue(int queueIndex) { return outputQueues[queueIndex]; }
}
