package p2p.deccen.core.protocols;

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
            sendPing(node, node, neighbors.getAll(), 1, pid);
            root = false;
        }

        for (Message rMessage : vec2) {
            processMessage(node, rMessage, pid);
        }

        vec2.clear();

        if (distances.size() == Network.size() - 1) {
            System.out.println("I am " + node.getID() + " and my Closeness Centrality is " + calculateClosenessCentrality());
        }
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

    private void processMessage(Node node, Message message, int pid) {
        if (message instanceof RequestMessage) {
            /** Reply with a Response message */
            RequestMessage rMessage = (RequestMessage) message;

            /** the source did already send a ping to me */
            if (vec1.contains(rMessage.originalSource))
                return;

            sendPong(rMessage.originalSource, node, rMessage.getValue(), pid);
            vec1.add(rMessage.originalSource);

            NeighborsProtocol neighborsLinkable = (NeighborsProtocol) node.getProtocol(FastConfig.getLinkable(pid));
            Node[] neighbors = neighborsLinkable.getAllExcept(new Node[] {rMessage.source, rMessage.originalSource});
            sendPing(rMessage.originalSource, node, neighbors, rMessage.getValue() + 1, pid);
        } else {
            ResponseMessage rMessage = (ResponseMessage) message;
            distances.put(rMessage.getSource(), rMessage.getValue());
        }

    }

    private void sendPong(Node originalSource, Node node, int value, int pid) {
        ResponseMessage rMessage = new ResponseMessage(value, node);
        ClosenessCentralityCD cced = (ClosenessCentralityCD) originalSource.getProtocol(pid);
        cced.addMessage(rMessage);
    }

    private void sendPing(Node originalSource, Node source, Node[] destinations, int distance, int pid) {
        for (Node destination : destinations) {
            if (destination.isUp())
                sendPing(originalSource, source, destination, distance, pid);
        }
    }

    private void sendPing(Node originalSource, Node source, Node destination, int distance, int pid) {
        RequestMessage rMessage = new RequestMessage(originalSource, source, distance);
        ClosenessCentralityCD cced = (ClosenessCentralityCD) destination.getProtocol(pid);
        cced.addMessage(rMessage);
    }

    private void addMessage(Message rMessage) {
        this.vec2.add(rMessage);
    }

    public Object clone() {
        ClosenessCentralityCD cced = (ClosenessCentralityCD) super.clone();
        cced.setFirstValue(new ArrayList<>());
        cced.setSecondValue(new ArrayList<>());
        cced.distances = new HashMap<>();
        return cced;
    }
}
