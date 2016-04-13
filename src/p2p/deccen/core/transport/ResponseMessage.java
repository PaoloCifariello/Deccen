package p2p.deccen.core.transport;

import peersim.core.Node;

/**
 * Created by paolocifariello.
 */
public class ResponseMessage implements Message {

    /**
     * Numeric value
     * of the
     * node src
     */
    private int value;
    /**
     * Source node
     */
    public Node source;

    /**
     * Create a new response message
     * with the specified numeric value and source node
     */
    public ResponseMessage(int value, Node source) {
        this.source = source;
        this.value = value;
    }

    @Override
    public int getValue() {
        return value;
    }

    public Node getSource() {
        return source;
    }
}