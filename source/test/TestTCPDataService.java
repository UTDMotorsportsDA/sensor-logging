package test;

import edu.utdallas.utdmotorsports.car.CarSimulator;

import java.io.IOException;
import java.io.InputStream;
import java.net.Socket;
import java.nio.charset.StandardCharsets;
import java.util.ArrayList;
import java.util.Scanner;
import java.util.concurrent.TimeUnit;

/**
 * Created by brian on 2/19/17.
 */
public class TestTCPDataService {
    public static void main(String args[]) {
        // open a client socket
        try(Socket sock = new Socket("127.0.0.1", 9897);
            InputStream is = sock.getInputStream();) {
            byte[] data;
//            InputStreamReader rd = new InputStreamReader(is)) {
            while(sock.isConnected() && !sock.isClosed()) {
                while(is.available() <= 0);
                data = new byte[is.available()];
                is.read(data);
                System.out.println(new String(data, StandardCharsets.US_ASCII));
            }
        } catch (IOException e) {
            e.printStackTrace();
            System.exit(1);
        }
    }
}
