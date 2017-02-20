package edu.utdallas.utdmotorsports.car;

import edu.utdallas.utdmotorsports.DataPoint;
import edu.utdallas.utdmotorsports.QueueMultiConsumer;
import edu.utdallas.utdmotorsports.Stoppable;

import java.io.IOException;
import java.io.OutputStream;
import java.net.ServerSocket;
import java.net.Socket;
import java.nio.charset.StandardCharsets;
import java.util.HashSet;
import java.util.Set;

/**
 * manage multiple client TCP connections
 * consume data points and send them to each client
 */
public class TCPDataService implements Runnable, Stoppable, QueueMultiConsumer<DataPoint> {
    // output streams for every client connected
    private Set<OutputStream> clientStreams;

    // server parameters
    private int servicePort;
    private Integer maxConnectionCount; // include the .wait() method

    // allow quittable infinite loop
    private boolean done = false;

    public TCPDataService(int servicePort, int maxConnectionCount) {
        // save parameters
        this.servicePort = servicePort;
        this.maxConnectionCount = maxConnectionCount;

        // instantiate the set of sockets
        clientStreams = new HashSet<>();
    }

    private void removeClientStream(OutputStream s) {
        clientStreams.remove(s);

        // wake up to see if it's time to accept connections again
        synchronized(maxConnectionCount) { maxConnectionCount.notify(); }
    }

    @Override
    public void run() {
        // open a new server socket if the live one throws an exception
        while(!done) {
            // open a ServerSocket and listen for connections
            try (ServerSocket ssock = new ServerSocket(servicePort)) {
                // allow quit() to close the socket
                quitSock = ssock;

                // accept multiple client connections
                while(!done) {
                    // accept a connection
                    Socket client = ssock.accept();

                    // add the connection's OutputStream to this instance's set
                    // don't let a single connection kill the server socket
                    try { clientStreams.add(client.getOutputStream()); System.out.println("new connection added");}
                    catch (IOException e) { e.printStackTrace(); }

                    // close the socket if maxConnectionCount reached
                    if(clientStreams.size() >= maxConnectionCount) break;
                }
            } catch (IOException e) {
                if(!done)
                    e.printStackTrace();
            }

            // adhere to maxConnectionCount
            synchronized (maxConnectionCount) {
                while (clientStreams.size() >= maxConnectionCount && !done)
                    try {
                        maxConnectionCount.wait();
                    } catch (InterruptedException e) { /* do nothing */ }
            }
        }
    }

    // immediately self-terminate (kind of like a destructor)
    ServerSocket quitSock = null;
    @Override
    public void quit() {
        // break out of any possible blocking call
        done = true;
        synchronized (maxConnectionCount) { maxConnectionCount.notify(); }
        if(quitSock != null)
            try { quitSock.close(); }
            catch (IOException e) { e.printStackTrace(); }

        // close all connections
        for(OutputStream s : clientStreams) {
            try { s.close(); clientStreams.remove(s); }
            catch (IOException e) { /* do nothing */ }
        }
    }

    // send the data point to every connection
    @Override
    public void processElement(DataPoint dataPoint) {
        String message = "{\"data\":\"" + dataPoint.toString() + "\"}\n";
        System.out.print(message);
        for(OutputStream s : clientStreams) {
            try { s.write(message.getBytes(StandardCharsets.US_ASCII)); }
            catch (IOException e) {
                try{ s.close(); }  // consider socket dead/broken if exception occurs, close if needed
                catch (IOException e1) { e.printStackTrace(); }
            }
            finally { removeClientStream(s); } // no sense keeping a dead socket
        }
    }
}
