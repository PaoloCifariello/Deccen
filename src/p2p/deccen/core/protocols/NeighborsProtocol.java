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
     * Parameter for initial capacity of the vector
     */
    public final static String PAR_INIT_CAPACITY = "capacity";

    /**
     * Default initial capacity of the vector
     */
    private final static int defaultInitialCapacity = 10;
    /**
     * size
     */
    private static int size;

    /**
     * Constructor
     */
    public NeighborsProtocol(String prefix) {
        /** get the parameter value from configuration file . */
        size = Configuration.getInt(prefix + "." + PAR_INIT_CAPACITY, defaultInitialCapacity);
        neighbors = new ArrayList<>(size);
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
        if (neighbors.contains(n) || neighbors.size() >= size)
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
        return this.neighbors.toArray(new Node[0]);
    }

    public Node[] getAllExcept(Node except) {
        ArrayList<Node> newNeighbors = new ArrayList<>(neighbors);
        newNeighbors.remove(except);
        return newNeighbors.toArray(new Node[0]);
    }

    public Node[] getAllExcept(Node[] except) {
        ArrayList<Node> newNeighbors = new ArrayList<>(neighbors);
        for (Node n : except) {
            newNeighbors.remove(n);
        }

        return newNeighbors.toArray(new Node[0]);
    }
}
