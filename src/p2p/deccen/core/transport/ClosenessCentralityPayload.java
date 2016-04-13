package p2p.deccen.core.transport;

import peersim.core.Node;

/**
 * Created by paolocifariello.
 */
public class ClosenessCentralityPayload implements Payload {
    private Node originalSource;
    private int distance;

    public ClosenessCentralityPayload(Node originalSource, int distance) {
        this.originalSource = originalSource;
        this.distance = distance;
    }

    public Node getOriginalSource() {
        return originalSource;
    }

    public int getDistance() {
        return distance;
    }
}
