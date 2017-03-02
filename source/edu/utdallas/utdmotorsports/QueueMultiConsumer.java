package edu.utdallas.utdmotorsports;

/**
 * Created by brian on 2/18/17.
 * Classes that use data from a Queue shall implement this interface.
 * Multiple QueueMultiConsumers can be registered to a QueueMultiProducer to all receive data from the same queue.
 * Generic "E" is the same data type as what goes through the queue.
 * Implementations should perform as little work as possible so as not to delay other consumers.
 */
public interface QueueMultiConsumer<E> {
    // do some work with an element from the queue
    void processElement(E e);
}
