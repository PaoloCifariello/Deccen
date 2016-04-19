package p2p.deccen.core.transport;

import peersim.core.Node;

/**
 * Created by paolocifariello.
 */
public class StressCentralityPayload extends ClosenessCentralityPayload {
    private int minPaths;
    private Node originalDestination;

    public StressCentralityPayload(Node originalSource, Node originalDestination, int distance, int minPaths) {
        super(originalSource, distance);
        this.originalDestination = originalDestination;
        this.minPaths = minPaths;
    }

    public int getMinPaths() {
        return minPaths;
    }

    public Node getOriginalDestination() {
        return originalDestination;
    }
}
