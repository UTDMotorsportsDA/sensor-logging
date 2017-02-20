package test;

import edu.utdallas.utdmotorsports.car.CarSimulator;

import java.io.IOException;
import java.io.InputStream;
import java.net.Socket;
import java.util.Scanner;
import java.util.concurrent.TimeUnit;

/**
 * Created by brian on 2/19/17.
 */
public class TestTCPDataService {
    public static void main(String args[]) {
        // open a client socket
        try(Socket sock = new Socket("127.0.0.1", 9897);
            InputStream is = sock.getInputStream();
            Scanner sc = new Scanner(is)) {
            while(true) {
                while(is.available() <= 0);
                System.out.println(sc.next());
            }
        } catch (IOException e) {
            e.printStackTrace();
            System.exit(1);
        }
    }
}
