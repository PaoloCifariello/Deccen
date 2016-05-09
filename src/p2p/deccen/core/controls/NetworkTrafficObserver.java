package p2p.deccen.core.controls;

import p2p.deccen.core.protocols.ClosenessCentralityCD;
import p2p.deccen.core.protocols.StressCentralityCD;
import peersim.config.Configuration;
import peersim.core.Control;
import peersim.core.Network;
import peersim.core.Node;

/**
 * Created by paolocifariello.
 */
public class NetworkTrafficObserver implements Control {

    private static final String CC_PROTOCOL = "ccProtocol";
    private static final String SC_PROTOCOL = "scProtocol";
    private static int cccdPid;
    private static int sccdPid;

    public NetworkTrafficObserver(String prefix) {
        cccdPid = Configuration.getPid(prefix + "." + CC_PROTOCOL);
        sccdPid = Configuration.getPid(prefix + "." + SC_PROTOCOL);
    }

    @Override
    public boolean execute() {

        int totalMessages = 0;

        for (int nodeId = 0; nodeId < Network.size(); nodeId++) {
            Node n = Network.get(nodeId);
            ClosenessCentralityCD cccd = (ClosenessCentralityCD) n.getProtocol(cccdPid);
            StressCentralityCD sccd = (StressCentralityCD) n.getProtocol(sccdPid);

            int sentMessages = cccd.sentMessages + sccd.sentMessages;

            System.out.println("Node " + nodeId + " has sent " + sentMessages);
            totalMessages += cccd.sentMessages + sccd.sentMessages;
        }

        System.out.println(totalMessages + " have been exchanged");

        return false;
    }
}

