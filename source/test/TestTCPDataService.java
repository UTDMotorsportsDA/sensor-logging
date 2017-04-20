import java.io.IOException;
import java.io.InputStream;
import java.net.Socket;
import java.nio.charset.StandardCharsets;
import java.util.concurrent.TimeUnit;

/**
 * this code is dirty, please don't make yours look this way
 */
public class TestTCPDataService {
    public static void main(String args[]) {
//        // find the service
//        String svcSockAddr;
//        try(MulticastSocket group = new MulticastSocket(6000)) {
//            String discoveryReq = "{\"discovery request\":{\"name\":\"Halley's Comet Data Transmitter\"}}";
//            byte[] data = discoveryReq.getBytes(StandardCharsets.US_ASCII);
//            group.send(new DatagramPacket(data, data.length));
//            DatagramPacket response = new DatagramPacket(new byte[1024], 1024);
//            String responseStr = null;
//            for(int i = 0; i < 100; ++i) {
//                group.receive(response);
//                responseStr = new String(response.getData(), 0, response.getLength(), StandardCharsets.US_ASCII);
//                if(responseStr.contains("\"discovery response\"")) break;
//                else responseStr = null;
//            }
//
//            JSONParser p = new JSONParser();
//            JSONObject resp = (JSONObject)p.parse(responseStr);
//            System.out.println(resp.toJSONString());
//            svcSockAddr = (JSONObject)resp.get("discovery response");
//        } catch (IOException e) {
//            e.printStackTrace();
//        } catch (ParseException e) {
//            e.printStackTrace();
//        }

        // open a client socket
        while (true) {
            System.out.println("opening new connection @ time " + System.currentTimeMillis() + " ms");
            try (Socket sock = new Socket(args[0], 9897);
                 InputStream is = sock.getInputStream()) {
                byte[] dataBuf = new byte[512];
                while (true) {
                    int numBytesRead = is.read(dataBuf);
                    if (numBytesRead > 0)
                        System.out.println(new String(dataBuf, 0, numBytesRead, StandardCharsets.US_ASCII));
                    else if (numBytesRead < 0)
                        break;
                }
            } catch (IOException e) {
                e.printStackTrace();
                try {
                    TimeUnit.SECONDS.sleep(3);
                } catch (InterruptedException e1) {
                    e1.printStackTrace();
                }
            }
        }
    }
}
