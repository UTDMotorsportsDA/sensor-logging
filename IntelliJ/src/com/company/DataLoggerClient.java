package com.company;

import java.io.IOException;
import java.io.PrintWriter;
import java.net.Socket;

/**
 * Created by brian on 10/16/16.
 */
public class DataLoggerClient implements Runnable {

    private String server = null;
    private int port = 0;
    private Sensor[] sensors = null;
    boolean done = false;

    public DataLoggerClient(String serverHostname, int serverPort, Sensor[] sensors) {
        server = serverHostname;
        port = serverPort;
        this.sensors = sensors;
    }

    @Override
    public void run() {
        // open a socket and writer to send data to the server
        try(Socket outgoingSocket = new Socket(server, port);
            PrintWriter outgoingWriter = new PrintWriter(outgoingSocket.getOutputStream(), true);) {

            System.out.println("Client is up.");

            while(true) {
                // read all sensors and write their data points to the socket
                for(Sensor s : sensors) {
                    // use .println(), else packet is not sent immediately
                    outgoingWriter.println(s.getDataPoint());
                }

                if(done) break;

                // time delay
                try {
                    Thread.sleep(500);
                } catch (InterruptedException e) {
                    e.printStackTrace();
                }
            }
        } catch (IOException e) {
            e.printStackTrace();
        }
    }

    // allow client to finish
    public void end() { done = true; }
}
