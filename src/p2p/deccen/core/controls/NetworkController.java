package p2p.deccen.core.controls;

import p2p.deccen.core.protocols.ClosenessCentralityCD;
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

        processCCMessages();
        return false;
    }

    private void processCCMessages() {
        for (int i = 0; i < Network.size(); i++) {
            Node n = Network.get(i);
            ClosenessCentralityCD cccd = (ClosenessCentralityCD) n.getProtocol(cccdPid);
            processCCMessages(n);
        }
    }

    private void processCCMessages(Node source) {
        ClosenessCentralityCD sourceCccd = (ClosenessCentralityCD) source.getProtocol(cccdPid);
        for (Message m : sourceCccd.outgoingMessages) {
            Node destination = m.getDestination();
            ClosenessCentralityCD destinationCccd = (ClosenessCentralityCD) destination.getProtocol(cccdPid);
            destinationCccd.incomingMessages.add(m);
        }
    }
}
