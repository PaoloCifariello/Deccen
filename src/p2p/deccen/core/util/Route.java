package p2p.deccen.core.util;

import peersim.core.Node;

/**
 * Created by paolocifariello.
 */
public class Route {

    Node source;
    Node destination;

    public Route(Node a, Node b) {
        source = a;
        destination = b;
    }

    public boolean equals(Object o) {
        if (o == null || !(o instanceof Route)) return false;
        Route other = (Route) o;
        return (source == other.source && destination == other.destination) ||
                        (source == other.destination && destination == other.source);
    }

    public int hashCode() {
        return new Long(source.getID() * destination.getID()).intValue(); // doesn't matter if some keys have same hash code
    }

    public Node getSource() {
        return source;
    }

    public Node getDestination() {
        return destination;
    }
}
