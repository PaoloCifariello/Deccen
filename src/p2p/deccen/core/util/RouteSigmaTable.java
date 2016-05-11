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

    public boolean fill(Node node) {
        for (Route r : getRoutes()) {
            Sigma s = getSigma(r);

            if (s.s2 == -1) {
                Node source = r.getSource();
                Node destination = r.getDestination();

                Sigma s1 = getSigma(source, node);
                Sigma s2 = getSigma(node, destination);

                if (s1 != null && s2 != null)
                    s.s2 = s1.s1 * s2.s1;
            }
        }

        return getSize() > 0 && isFilled();
    }

    public double computeBetwennessCentrality(Node me) {
        double bc = 0;

        for (Route r : getRoutes()) {
            if (r.getSource() != me && r.getDestination() != me) {
                Sigma s = getSigma(r);
                bc += ((double) s.s2 / (double) s.s1);
            }
        }

        return bc * 2;
    }

    public int computeStressCentrality(Node me) {
        int sc = 0;

        for (Route r : getRoutes()) {
            if (r.getSource() != me && r.getDestination() != me) {
                Sigma s = getSigma(r);
                sc += s.s2;
            }
        }

        return sc * 2;
    }
}
