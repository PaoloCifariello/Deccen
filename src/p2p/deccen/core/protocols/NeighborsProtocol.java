package p2p.deccen.core.protocols;

/**
 * Created by paolocifariello.
 */

import peersim.config.Configuration;
import peersim.core.Linkable;
import peersim.core.Node;
import peersim.core.Protocol;

import java.util.ArrayList;

/** A protocol that stores links  */
public class NeighborsProtocol implements Protocol, Linkable {
    /**
     * Neighbors
     */
    protected ArrayList<Node> neighbors;

    /**
     * Constructor
     */
    public NeighborsProtocol(String prefix) {
        /** get the parameter value from configuration file . */
        neighbors = new ArrayList<>();
    }

    /**
     * Returns true if the given node is a member of the neighbor set .
     */

    public boolean contains(Node n) {
        return neighbors.contains(n);
    }

    /**
     * Add a neighbor to the current set of neighbors.
     */
    public boolean addNeighbor(Node n) {
        if (this.contains(n))
            return false;

        neighbors.add(n);
        return true;
    }

    /**
     * Returns the neighbor with the given index.
     */
    public Node getNeighbor(int i) {
        return neighbors.get(i);
    }

    /**
     * Returns the size of the neighbor list.
     */
    public int degree() {
        return neighbors.size();
    }

    /**
     * Cloneable
     */
    public Object clone() {
        NeighborsProtocol np = null;
        try {
            np = (NeighborsProtocol) super.clone();
        } catch (CloneNotSupportedException e) {
        } // never happens

        np.neighbors = new ArrayList<>() ;
        return np;
    }

    /**
     * A possibility for optimization .
     */
    public void pack() {
    }

    @Override
    public void onKill() {

    }

    public Node[] getAll() {
        return this.neighbors.toArray(new Node[neighbors.size()]);
    }

    public Node[] getAllExcept(Node except) {
        ArrayList<Node> newNeighbors = new ArrayList<>(neighbors);
        newNeighbors.remove(except);
        return newNeighbors.toArray(new Node[newNeighbors.size()]);
    }

    public Node[] getAllExcept(Node[] except) {
        ArrayList<Node> newNeighbors = new ArrayList<>(neighbors);
        for (Node n : except) {
            newNeighbors.remove(n);
        }

        return newNeighbors.toArray(new Node[newNeighbors.size()]);
    }
}
