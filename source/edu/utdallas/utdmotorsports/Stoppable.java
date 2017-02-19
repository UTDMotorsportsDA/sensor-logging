package edu.utdallas.utdmotorsports;

/**
 * simple additional helper for threaded classes
 */
public interface Stoppable {
    // instance should end its work and self-terminate
    public void quit();
}
