package p2p.deccen.core.protocols;

import p2p.deccen.core.transport.Message;
import p2p.deccen.core.transport.RequestMessage;
import p2p.deccen.core.transport.ResponseMessage;
import p2p.deccen.core.transport.StressCentralityPayload;
import p2p.deccen.core.util.Routing;
import p2p.deccen.core.values.DoubleVectorHolder;
import peersim.cdsim.CDProtocol;
import peersim.config.Configuration;
import peersim.config.FastConfig;
import peersim.core.Node;

import java.util.ArrayList;
import java.util.HashMap;

/**
 * Created by paolocifariello.
 */
public class StressCentralityCD extends DoubleVectorHolder<Node, Message>
        implements CDProtocol {

    private static final String CC_PROTOCOL = "ccProtocol";
    private static int cccdPid;

    private boolean firstCycle = false;
    private HashMap<Node, ArrayList<RequestMessage>> inQueue = new HashMap<>();

    private Routing routing = new Routing();

    public StressCentralityCD(String prefix) {
        cccdPid = Configuration.getPid(prefix + "." + CC_PROTOCOL);
    }

    @Override
    public void nextCycle(Node node, int pid) {
        ClosenessCentralityCD cccd = (ClosenessCentralityCD) node.getProtocol(cccdPid);

        // distances are stable from Closeness Centrality protocol, so we can start with stress centrality
        if (cccd.isStable() && firstCycle) {
            NeighborsProtocol neighbors = (NeighborsProtocol) node.getProtocol(FastConfig.getLinkable(pid));
            sendPing(node, neighbors.getAll(), node, 1, 1, pid);
            this.firstCycle = false;
        }

        for (Node originalSource : inQueue.keySet()) {
            processMessagesFrom(originalSource, node, pid);
        }

        vec2.clear();
    }

    private void sendPing(Node source, Node[] destinations, Node originalSource, int distance, int minPaths, int pid) {
        for (Node destination : destinations) {
            if (destination.isUp())
                sendPing(source, destination, originalSource, distance, minPaths, pid);
        }
    }

    private void sendPing(Node source, Node destination, Node originalSource, int distance, int minPaths, int pid) {
        RequestMessage rMessage = new RequestMessage(source, destination, new StressCentralityPayload(originalSource, distance, minPaths));
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

    private void processMessagesFrom(Node originalSource, Node node, int pid) {
        ArrayList<RequestMessage> queue = inQueue.get(originalSource);

        if (!queue.isEmpty()) {
            int minPathsFromSource = 0;

//            for (RequestMessage rMessage : queue) {
//                minPathsFromSource += rMessage.getDistance();
//            }

//            for (RequestMessage rMessage : queue) {
//                ResponseMessage responseMessage = new ResponseMessage(minPathsFromSource)
//            }

            queue.clear();

            NeighborsProtocol neighbors = (NeighborsProtocol) node.getProtocol(FastConfig.getLinkable(pid));
            // forward the message to all neighbors
//            sendPing(originalSource, node, neighbors.getAll(), minPathsFromSource, pid);
            // tells to originalSource # of min paths from originalSource to node
            sendPong(originalSource, node, minPathsFromSource, pid);
        }
    }

    private void sendPong(Node originalSource, Node node, int value, int pid) {
        //ResponseMessage rMessage = new ResponseMessage(value, node);
        //StressCentralityCD sccd = (StressCentralityCD) originalSource.getProtocol(pid);
        //sccd.addReplyMessage(rMessage);
    }

    private void addReplyMessage(ResponseMessage rMessage) {
        vec2.add(rMessage);
    }

    public Object clone() {
        StressCentralityCD cced = (StressCentralityCD) super.clone();
        cced.setFirstValue(new ArrayList<>());
        cced.setSecondValue(new ArrayList<>());
        cced.inQueue = new HashMap<>();
        cced.routing = new Routing();
        cced.firstCycle = true;
        return cced;
    }
}
