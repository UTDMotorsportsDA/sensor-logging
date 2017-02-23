import java.io.IOException;
import java.io.InputStream;
import java.net.Socket;
import java.nio.charset.StandardCharsets;

/**
 * Created by brian on 2/19/17.
 */
public class TestTCPDataService {
    public static void main(String args[]) {
        // open a client socket
        try(Socket sock = new Socket("192.168.2.104", 9897);
            InputStream is = sock.getInputStream();) {
            byte[] dataBuf = new byte[512];
            while(true) {
                int numBytesRead = is.read(dataBuf);
                if(numBytesRead > 0)
                    System.out.println(new String(dataBuf, 0, numBytesRead, StandardCharsets.US_ASCII));
                else if(numBytesRead < 0)
                    break;
            }
        } catch (IOException e) {
            e.printStackTrace();
            System.exit(1);
        }
    }
}
