package p2p.deccen.core.transport;

import peersim.core.Node;

/**
 * Created by paolocifariello.
 */
public class ResponseMessage extends Message {

    /**
     * Create a new response message
     * with the specified numeric value and source node
     */
    public ResponseMessage(Node source, Node destination, ClosenessCentralityPayload ccp) {
        super(source, destination, ccp);
    }
}