package p2p.deccen.core.protocols;

import p2p.deccen.core.transport.ClosenessCentralityPayload;
import p2p.deccen.core.transport.Message;
import p2p.deccen.core.transport.RequestMessage;
import p2p.deccen.core.transport.ResponseMessage;
import p2p.deccen.core.values.DoubleVectorHolder;
import peersim.cdsim.CDProtocol;
import peersim.config.FastConfig;
import peersim.core.Network;
import peersim.core.Node;

import java.util.ArrayList;
import java.util.HashMap;

/**
 * This implements the calculation of Closeness Centrality of a node v
 *
 * DoubleVectorHolder is used to mantain a double vector of values containing>
 * - list of sources from which i already received a PING
 * - list of messages, used to simulate a cycle-based protocol, with message exchanging
 */
public class ClosenessCentralityCD extends DoubleVectorHolder<Node, Message>
        implements CDProtocol {

    public HashMap<Node, Integer> distances = new HashMap<>();
    public boolean root = false;
    private boolean stable = false;

    public ClosenessCentralityCD(String prefix) {
    }

    public boolean isStable() {
        return stable;
    }

    public void setRoot() {
        this.root = true;
    }

    /**
     * This is the standard method the define periodic activity.
     * The frequency of execution of this method is defined by a
     * {@link peersim.edsim.CDScheduler} component in the configuration.
     */
    public void nextCycle(Node node, int pid) {
        if (root) {
            NeighborsProtocol neighbors = (NeighborsProtocol) node.getProtocol(FastConfig.getLinkable(pid));
            sendPing(node, neighbors.getAll(), node, 1, pid);
            root = false;
        }

        int distancesSize = distances.size();

        for (Message rMessage : vec2) {
            processMessage(node, rMessage, pid);
        }

        vec2.clear();

        /**
         * when a node A does not add a new node to the list of reachable ones at cycle k,
         * it will not add any other node in further cycles, so if in a cycle I don't add any node to distances (except
         * for the very first cycle) the protocol on this node is considered as 'stable'
         */
        if (distancesSize != 0 && distances.size() == distancesSize) {
            stable = true;
            //System.out.println("I am " + node.getID() + " and my Closeness Centrality is " + calculateClosenessCentrality());
        }
    }

    private void sendPing(Node source, Node[] destinations, Node originalSource, int distance, int pid) {
        for (Node destination : destinations) {
            if (destination.isUp())
                sendPing(source, destination, originalSource, distance, pid);
        }
    }

    private void sendPing(Node source, Node destination, Node originalSource, int distance, int pid) {
        RequestMessage rMessage = new RequestMessage(source, destination, new ClosenessCentralityPayload(originalSource, distance));
        ClosenessCentralityCD cced = (ClosenessCentralityCD) destination.getProtocol(pid);
        cced.addMessage(rMessage);
    }

    private void processMessage(Node node, Message message, int pid) {
        ClosenessCentralityPayload ccp = (ClosenessCentralityPayload) message.getPayload();

        if (message instanceof RequestMessage) {
            /** the source did already send a ping to me */
            if (vec1.contains(ccp.getOriginalSource()))
                return;

            sendPong(ccp.getOriginalSource(), node, ccp.getDistance(), pid);
            vec1.add(ccp.getOriginalSource());

            NeighborsProtocol neighborsLinkable = (NeighborsProtocol) node.getProtocol(FastConfig.getLinkable(pid));
            Node[] neighbors = neighborsLinkable.getAllExcept(new Node[] {message.getSource(), ccp.getOriginalSource()});
            sendPing(node, neighbors, ccp.getOriginalSource(), ccp.getDistance() + 1, pid);
        } else {
            distances.put(message.getSource(), ccp.getDistance());
        }

    }

    private void sendPong(Node originalSource, Node node, int distance, int pid) {
        ResponseMessage rMessage = new ResponseMessage(node, originalSource, new ClosenessCentralityPayload(node, distance));
        ClosenessCentralityCD cced = (ClosenessCentralityCD) originalSource.getProtocol(pid);
        cced.addMessage(rMessage);
    }

    private int calculateClosenessCentrality() {
        int totalDistance = 0;
        int totalNodes = 0;

        for (Node n : distances.keySet()) {
            totalDistance+= distances.get(n);
            totalNodes++;
        }

        return totalDistance/totalNodes;
    }

    private void addMessage(Message rMessage) {
        this.vec2.add(rMessage);
    }

    public int getDistance(Node node) {
        if (distances.containsKey(node))
            return distances.get(node);

        return -1;
    }

    public Object clone() {
        ClosenessCentralityCD cced = (ClosenessCentralityCD) super.clone();
        cced.setFirstValue(new ArrayList<>());
        cced.setSecondValue(new ArrayList<>());
        cced.distances = new HashMap<>();
        return cced;
    }
}