package p2p.deccen.core.controls;

import p2p.deccen.core.protocols.ClosenessCentralityCD;
import p2p.deccen.core.protocols.NetworkedProtocol;
import p2p.deccen.core.transport.Message;
import peersim.config.Configuration;
import peersim.core.Control;
import peersim.core.Network;
import peersim.core.Node;

import java.util.ArrayList;

/**
 * Created by paolocifariello.
 */
public class NetworkController implements Control {

    private static final String CC_PROTOCOL = "ccProtocol";
    private static int cccdPid;
    private static final String SC_PROTOCOL = "scProtocol";
    private static int sccdPid;


    public NetworkController(String prefix) {
        cccdPid = Configuration.getPid(prefix + "." + CC_PROTOCOL);
        sccdPid = Configuration.getPid(prefix + "." + SC_PROTOCOL);
    }


    @Override
    public boolean execute() {
        processMessages(cccdPid);
        processMessages(sccdPid);
        return false;
    }

    private void processMessages(int protocolPid) {
        for (int i = 0; i < Network.size(); i++) {
            Node n = Network.get(i);
            processMessages(n, protocolPid);
        }
    }

    private void processMessages(Node source, int protocolPid) {
        NetworkedProtocol sourceProtocol = (NetworkedProtocol) source.getProtocol(protocolPid);

        for (Message m : sourceProtocol.outgoingMessages) {
            Node destination = m.getDestination();
            NetworkedProtocol destinationProtocol = (NetworkedProtocol) destination.getProtocol(protocolPid);
            destinationProtocol.incomingMessages.add(m);
        }

        sourceProtocol.outgoingMessages.clear();
    }
}
