package p2p.deccen.core.transport;

import peersim.core.Node;

/**
 * Created by paolocifariello.
 */
public abstract class Message {

    public Message(Node source, Node destination, Payload payload) {
        this.source = source;
        this.destination = destination;
        this.payload = payload;
    }

    /**
     * Source node
     */
    protected Node source;

    /**
     * Destination node
     */
    protected Node destination;


    protected Payload payload;

    public Node getSource() {
        return source;
    }

    public Node getDestination() {
        return destination;
    }
    public Payload getPayload() {
        return payload;
    }
}
