package p2p.deccen.core.transport;

import peersim.core.Node;

/**
 * Created by paolocifariello.
 */
public class StressCentralityPayload extends ClosenessCentralityPayload {
    private int minPaths;

    public StressCentralityPayload(Node originalSource, int distance, int minPaths) {
        super(originalSource, distance);
        this.minPaths = minPaths;
    }

    public int getMinPaths() {
        return minPaths;
    }
}
