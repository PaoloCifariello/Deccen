package p2p.deccen.core.util;

import peersim.core.Node;

import java.util.ArrayList;
import java.util.HashMap;

/**
 * Created by paolocifariello.
 */
public class Routing {

    private HashMap<Node, ArrayList<Node>> routingTable = new HashMap<>();

    public ArrayList<Node> getRoute(Node node) {
        if (routingTable.containsKey(node))
            return routingTable.get(node);

        return new ArrayList<>();
    }

    public void addRoute(Node originalSource, Node source) {
        ArrayList<Node> route;

        if (routingTable.containsKey(originalSource)) {
            route = routingTable.get(originalSource);
        } else {
            route = new ArrayList<>();
        }

        if (!route.contains(source))
            route.add(source);

        routingTable.put(originalSource, route);
    }
}
