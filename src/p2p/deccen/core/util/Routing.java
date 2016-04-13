package p2p.deccen.core.util;

import peersim.core.Node;

import java.util.HashMap;

/**
 * Created by paolocifariello.
 */
public class Routing {

    private HashMap<Node, Node> routingTable = new HashMap<>();

    public Node getRoute(Node a) {
        return routingTable.get(a);
    }
}
