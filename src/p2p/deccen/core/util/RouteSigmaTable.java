package p2p.deccen.core.util;

import peersim.core.Node;

import java.util.HashMap;
import java.util.Set;

/**
 * Created by paolocifariello.
 */
public class RouteSigmaTable {

    private HashMap<Route, Sigma> routes = new HashMap<>();

    public Sigma getSigma(Node a, Node b) {
        return getSigma(new Route(a, b));
    }

    public void addRoute(Node a, Node b, Sigma s) {
        this.routes.put(new Route(a, b), s);
    }

    public Set<Route> getRoutes() {
        return routes.keySet();
    }

    public boolean containsRoute(Node originalSource, Node originalDestination) {
        return routes.containsKey(new Route(originalSource, originalDestination));
    }

    public Sigma getSigma(Route r) {
        return routes.get(r);
    }

    public boolean isFilled() {
        for (Route r : getRoutes()) {
            Sigma s = getSigma(r);
            if (s.s1 == -1 || s.s2 == -1)
                return false;
        }

        return true;
    }

    public int getSize() {
        return routes.size();
    }
}
