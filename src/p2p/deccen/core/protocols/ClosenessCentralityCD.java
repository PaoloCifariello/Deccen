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
 * This implements the computation of Closeness Centrality of a node v
 *
 */
public class ClosenessCentralityCD extends NetworkedProtocol
        implements CDProtocol {

    public HashMap<Node, Integer> distances = new HashMap<>();
    public boolean root = false;
    public double closenessCentrality = 0;

    /** this list contains sources already discovered by this node (already got a ping message from those) */
    private ArrayList<Node> discoveredSources = new ArrayList<>();

    public ClosenessCentralityCD(String prefix) {
    }

    public void setRoot() {
        this.root = true;
    }

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

        closenessCentrality = this.calculateClosenessCentrality();
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

    private double calculateClosenessCentrality() {
        double totalDistance = 0;
        int totalNodes = 1;

        for (Node n : distances.keySet()) {
            totalDistance += distances.get(n);
            totalNodes++;
        }

        return totalDistance/totalNodes;
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