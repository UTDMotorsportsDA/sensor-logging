package edu.utdallas.utdmotorsports.car;

import edu.utdallas.utdmotorsports.Stoppable;
import org.json.simple.JSONObject;
import org.json.simple.parser.JSONParser;
import org.json.simple.parser.ParseException;

import java.io.IOException;
import java.net.*;
import java.nio.charset.StandardCharsets;
import java.util.Calendar;

// responds to requests on the multicast group with TCP connection information
public class ServiceDiscoveryResponder implements Runnable, Stoppable {
    // number of messages to test over the network to find the machine's IP address
    private static final int IP_CHECK_COUNT = 5;

    // response info
    private String serviceName; // what the service is called
    private int servicePort; // the service's port
    private String parametersLocation; // the service's extra info file
    JSONObject responseObject;

    // listening info
    private InetAddress multicastGroup;
    private int multicastPort;

    // signal when to end
    private boolean done = false;

    // set running parameters
    public ServiceDiscoveryResponder(InetAddress multicastGroup, int multicastPort, String serviceName, int serverSocketPort, String parametersLocation) {
        this.multicastGroup = multicastGroup;
        this.multicastPort = multicastPort;
        this.serviceName = serviceName;
        this.servicePort = serverSocketPort;
        this.parametersLocation = parametersLocation;
    }

    private String currentTimeString() {
        Calendar cal = Calendar.getInstance();
        return cal.get(Calendar.HOUR_OF_DAY) + ":" + cal.get(Calendar.MINUTE) +":" + cal.get(Calendar.SECOND) + "." + cal.get(Calendar.MILLISECOND);
    }

    private String getResponse() {
        ((JSONObject)responseObject.get("discovery response")).put("clock", currentTimeString());
        return responseObject.toString();
    }

    private static InetAddress probeForMyAddress(DatagramSocket listenSocket, DatagramPacket sendPacket) {
        // address found by the routine
        InetAddress thisAddress = null;

        try(DatagramSocket outSock = new DatagramSocket()) {
            // one packet for probing, parameters for listening
            DatagramPacket rcvPkt = new DatagramPacket(new byte[512], 512);

            // determine this machine's IP address by generating preliminary traffic
            int oldTimeout = listenSocket.getSoTimeout();
            listenSocket.setSoTimeout(5000); // make sure we time out to be able to retry
            outer:
            for (int i = IP_CHECK_COUNT; i > 0; --i) {
                // send this string to the group
                String probeString = Double.toString(Math.random());
                byte[] probeBytes = probeString.getBytes(StandardCharsets.US_ASCII);
                sendPacket.setData(probeBytes);
                outSock.send(sendPacket);

                // try to receive the same probe string
                for (int j = IP_CHECK_COUNT; j > 0; --j) {
                    listenSocket.receive(rcvPkt);
                    String rcvString = new String(rcvPkt.getData(), 0, rcvPkt.getLength(), StandardCharsets.US_ASCII);
                    if (probeString.equals(rcvString)) {
                        if (thisAddress == null)
                            thisAddress = rcvPkt.getAddress();
                        else if (thisAddress != rcvPkt.getAddress()) {
                            thisAddress = null; // forget any previous address
                            break outer; // got conflicting matches, probing failed
                        }
                        continue outer; // so far so good, confirm this address again
                    }
                }
            }
            // restore timeout
            listenSocket.setSoTimeout(oldTimeout);
        } catch (IOException e) {
            e.printStackTrace();
            System.exit(1);
        }

        // will be null if acceptable address not found
        return thisAddress;
    }

    @Override
    public void run() {
        try(DatagramSocket outgoingSocket = new DatagramSocket();
            MulticastSocket listenSocket = new MulticastSocket(multicastPort)) {
            // join the multicast group
            listenSocket.joinGroup(multicastGroup);

            // hold a packet to receive messages from the group
            DatagramPacket rcvPkt = new DatagramPacket(new byte[512], 512);

            // grab the address this machine holds on the multicast group
            InetAddress thisAddress = probeForMyAddress(listenSocket, new DatagramPacket(new byte[0], 0, multicastGroup, multicastPort));
            listenSocket.setInterface(thisAddress); // good practice

            // make sure listenSocket times out if it's been too long (1 second)
            listenSocket.setSoTimeout(1000);

            // debug
            System.out.println("ServiceDiscoveryResponder Operating at address " + thisAddress.getHostName());

            // prepare a response for requestors
            responseObject = new JSONObject();
            JSONObject contents = new JSONObject();
            contents.put("name", serviceName);
            contents.put("location", thisAddress.getHostName() + ":" + servicePort);
            contents.put("params", parametersLocation);
            responseObject.put("discovery response", contents);

            // debug
            System.out.println("example response:\n" + getResponse());

            // persistent JSONParser to decode messages
            JSONParser parser = new JSONParser();
            while(!done) {
                // get a packet
                try { listenSocket.receive(rcvPkt); }
                catch (SocketTimeoutException e) { continue; }

                // extract contained message
                String message = new String(rcvPkt.getData(), 0, rcvPkt.getLength(), StandardCharsets.US_ASCII);

                Object obj;
                try {
                    obj = parser.parse(message);
                } catch (ParseException e) {
                    continue; // not JSON data
                }

                // if this is a JSONObject (like we expect a discovery request to be)
                if(obj instanceof JSONObject) {
                    // if the object has a sole discovery response field
                    JSONObject jObj = (JSONObject)obj;
                    // make sure there's one map entry, make sure it's a discovery request, verify service name
                    if(jObj.size() == 1 && null != (contents = (JSONObject)jObj.get("discovery request")) && contents.get("name").equals(serviceName)) {
                        System.out.println("discovery request received: \n\t" + message);
                        // respond directly to requestor
                        byte[] responseData = getResponse().getBytes(StandardCharsets.US_ASCII);
                        outgoingSocket.send(new DatagramPacket(responseData, responseData.length, rcvPkt.getAddress(), multicastPort));
                    }
                }
            }
        } catch (IOException e) {
            if(!done) {
                e.printStackTrace();
            }
        }
    }

    // stop the thread for it to self-terminate
    public void quit() { done = true; }
}
