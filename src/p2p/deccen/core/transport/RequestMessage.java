package p2p.deccen.core.transport;

import peersim.core.Node;

/**
 * Created by paolocifariello.
 */
public class RequestMessage extends Message {

    public RequestMessage(Node source, Node destination, ClosenessCentralityPayload ccp) {
        super(source, destination, ccp);
    }
}
