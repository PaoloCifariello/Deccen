package p2p.deccen.core.protocols;

import p2p.deccen.core.transport.Message;
import p2p.deccen.core.transport.RequestMessage;
import p2p.deccen.core.transport.ResponseMessage;
import p2p.deccen.core.values.DoubleVectorHolder;
import peersim.cdsim.CDProtocol;
import peersim.config.FastConfig;
import peersim.core.Node;

import java.util.ArrayList;
import java.util.HashMap;

/**
 * Created by paolocifariello.
 */
public class StressCentralityCD extends DoubleVectorHolder<Node, Message>
        implements CDProtocol {

    private boolean firstCycle = false;
    private HashMap<Node, ArrayList<RequestMessage>> inQueue = new HashMap<>();

    @Override
    public void nextCycle(Node node, int pid) {
        if (firstCycle) {
            NeighborsProtocol neighbors = (NeighborsProtocol) node.getProtocol(FastConfig.getLinkable(pid));
            sendPing(node, node, neighbors.getAll(), 1, pid);
            this.firstCycle = false;
        }

        for (Node originalSource : inQueue.keySet()) {
            processMessagesFrom(originalSource, node, pid);
        }

        vec2.clear();
    }

    private void processMessagesFrom(Node originalSource, Node node, int pid) {
        ArrayList<RequestMessage> queue = inQueue.get(originalSource);

        if (!queue.isEmpty()) {
            int minPathsFromSource = 0;

            for (RequestMessage rMessage : queue) {
                minPathsFromSource += rMessage.getValue();
            }

            queue.clear();

            NeighborsProtocol neighbors = (NeighborsProtocol) node.getProtocol(FastConfig.getLinkable(pid));
            // forward the message to all neighbors
            sendPing(originalSource, node, neighbors.getAll(), minPathsFromSource, pid);
            // tells to originalSource # of min paths from originalSource to node
            sendPong(originalSource, node, minPathsFromSource, pid);
        }
    }

    private void sendPong(Node originalSource, Node node, int value, int pid) {
        ResponseMessage rMessage = new ResponseMessage(value, node);
        StressCentralityCD sccd = (StressCentralityCD) originalSource.getProtocol(pid);
        sccd.addReplyMessage(rMessage);
    }

    private void addReplyMessage(ResponseMessage rMessage) {
        vec2.add(rMessage);
    }

    private void sendPing(Node originalSource, Node source, Node[] destinations, int minPaths, int pid) {
        for (Node destination : destinations) {
            if (destination.isUp())
                sendPing(originalSource, source, destination, minPaths, pid);
        }
    }

    private void sendPing(Node originalSource, Node source, Node destination, int minPaths, int pid) {
        RequestMessage rMessage = new RequestMessage(originalSource, source, minPaths);
        StressCentralityCD sscd = (StressCentralityCD) destination.getProtocol(pid);
        sscd.addRequestMessage(originalSource, rMessage);
    }

    private void addRequestMessage(Node originalSource, RequestMessage rMessage) {
        ArrayList<RequestMessage> queue;

        /** initialization of the queue associated to originalSource */
        if (!inQueue.containsKey(originalSource)) {
            queue = new ArrayList<>();
        } else { // the queue was already initialized
            queue = inQueue.get(originalSource);

            /** if the queue was already initialized, but it is empty, it was already used to keep information
                about min paths from originalSource (that was of minimum distance) */
            if (queue.isEmpty())
                return;
        }

        queue.add(rMessage);
        inQueue.put(originalSource, queue);
    }
}
