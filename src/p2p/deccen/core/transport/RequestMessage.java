package p2p.deccen.core.transport;

import peersim.core.Node;

/**
 * Created by paolocifariello.
 */
public class RequestMessage implements Message {

    /**
     * Numeric value of the node src
     */
    private int value;
    /**
     * Source node
     */
    public Node source;

    /**
     * Original source node
     */
    public Node originalSource;

    /**
     * Create a new request message with the
     * specified numeric value and source node
     */
    public RequestMessage(Node originalSource, Node source, int value) {
        this.originalSource = originalSource;
        this.source = source;
        this.value = value;
    }

    @Override
    public int getValue() {
        return value;
    }
}
