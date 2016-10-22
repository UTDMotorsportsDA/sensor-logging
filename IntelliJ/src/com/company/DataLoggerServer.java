package com.company;

import java.io.IOException;
import java.net.ServerSocket;
import java.net.Socket;
import java.util.Scanner;

/**
 * Created by brian on 10/16/16.
 */
public class DataLoggerServer implements Runnable {

    // desired port
    private int port = 0;

    // completion condition (allow server to quit)
    private boolean done = false;

    private Scanner in = null;

    public DataLoggerServer(int port) {
        this.port = port;
    }

    @Override
    public void run() {
        // open a listening socket, wait for client, open a client socket
        try(ServerSocket serverSocket = new ServerSocket(port);
            Socket clientSocket = serverSocket.accept();) {

            in = new Scanner(clientSocket.getInputStream());

            System.out.println("Server is up.");

            // print any information from the socket
            while(!done) {
                if(in.hasNext())
                    System.out.println(in.next());
                else
                    Thread.sleep(500);
            }

        } catch (IOException e) {
            e.printStackTrace();
        } catch (InterruptedException e) {
            e.printStackTrace();
        }
    }

    // allow server to quit
    public void end() { done = true; if(in != null) in.close(); }
}
