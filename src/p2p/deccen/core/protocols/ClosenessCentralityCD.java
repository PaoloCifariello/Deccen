package p2p.deccen.core.protocols;

import p2p.deccen.core.transport.ClosenessCentralityPayload;
import p2p.deccen.core.transport.Message;
import p2p.deccen.core.transport.RequestMessage;
import p2p.deccen.core.transport.ResponseMessage;
import peersim.cdsim.CDProtocol;
import peersim.config.FastConfig;
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
public class ClosenessCentralityCD extends NetworkedProtocol
        implements CDProtocol {

    public HashMap<Node, Integer> distances = new HashMap<>();
    public boolean root = false;
    public int sentMessages = 0;

    /** this list contains sources already discovered by this node (already got a ping message from those) */
    private ArrayList<Node> discoveredSources = new ArrayList<>();

    public ClosenessCentralityCD(String prefix) {
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

        for (Message rMessage : incomingMessages) {
            processMessage(node, rMessage, pid);
        }

        incomingMessages.clear();
    }

    private void sendPing(Node source, Node[] destinations, Node originalSource, int distance, int pid) {
        for (Node destination : destinations) {
            if (destination.isUp())
                sendPing(source, destination, originalSource, distance, pid);
        }
    }

    private void sendPing(Node source, Node destination, Node originalSource, int distance, int pid) {
        RequestMessage rMessage = new RequestMessage(source, destination, new ClosenessCentralityPayload(originalSource, distance));
        sendMessage(rMessage);
    }

    private void processMessage(Node node, Message message, int pid) {
        ClosenessCentralityPayload ccp = (ClosenessCentralityPayload) message.getPayload();

        if (message instanceof RequestMessage) {
            /** the source did already send a ping to me */
            if (discoveredSources.contains(ccp.getOriginalSource()))
                return;

            sendPong(ccp.getOriginalSource(), node, ccp.getDistance(), pid);
            discoveredSources.add(ccp.getOriginalSource());

            NeighborsProtocol neighborsLinkable = (NeighborsProtocol) node.getProtocol(FastConfig.getLinkable(pid));
            Node[] neighbors = neighborsLinkable.getAllExcept(new Node[] {message.getSource(), ccp.getOriginalSource()});
            sendPing(node, neighbors, ccp.getOriginalSource(), ccp.getDistance() + 1, pid);
        } else {
            Node source = message.getSource();
            int distance = ccp.getDistance();
            if (!distances.containsKey(source) || (distances.get(source) > distance)) {
                distances.put(source, distance);
            }
        }

    }

    private void sendPong(Node originalSource, Node node, int distance, int pid) {
        ResponseMessage rMessage = new ResponseMessage(node, originalSource, new ClosenessCentralityPayload(node, distance));
        sendMessage(rMessage);
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
        this.incomingMessages.add(rMessage);
    }

    public int getDistance(Node node) {
        if (distances.containsKey(node))
            return distances.get(node);

        return -1;
    }

    public Object clone() {
        ClosenessCentralityCD cccd = (ClosenessCentralityCD) super.clone();

        cccd.distances = new HashMap<>();
        cccd.discoveredSources = new ArrayList<>();

        return cccd;
    }
}