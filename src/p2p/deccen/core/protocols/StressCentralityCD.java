package p2p.deccen.core.protocols;

import p2p.deccen.core.transport.RequestMessage;
import p2p.deccen.core.transport.ResponseMessage;
import p2p.deccen.core.transport.StressCentralityPayload;
import p2p.deccen.core.util.Route;
import p2p.deccen.core.util.RouteSigmaTable;
import p2p.deccen.core.util.Routing;
import p2p.deccen.core.util.Sigma;
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
public class StressCentralityCD extends DoubleVectorHolder<Node, ResponseMessage>
        implements CDProtocol {

    private static final String CC_PROTOCOL = "ccProtocol";
    private static int cccdPid;

    private boolean firstCycle = false;
    private HashMap<Node, ArrayList<RequestMessage>> inQueue = new HashMap<>();
    private RouteSigmaTable rst = new RouteSigmaTable();

    private Routing routing = new Routing();

    private int stressCentrality;
    private double betweennessCentrality;

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

        /** process request messages for each original source */
        for (Node originalSource : inQueue.keySet()) {
            processMessagesFrom(originalSource, node, pid);
        }

        /** forward back response messages */
        for (ResponseMessage rMessage : vec2) {
            processResponseMessage(node, rMessage, pid);
        }

        /** RouteSigmaTable is completed partially at each cycle */
        fillRouteSigmaTable(node);

        vec2.clear();
    }

    private void processResponseMessage(Node node, ResponseMessage rMessage, int pid) {
        StressCentralityPayload scp = (StressCentralityPayload) rMessage.getPayload();
        Node originalSource = scp.getOriginalSource();
        Node originalDestination = scp.getOriginalDestination();
        /** node is on at least 1 min path from originalSource to originalDestination */

        if (node.equals(originalSource) && !rst.containsRoute(originalSource, originalDestination)) { // I am originalSource
            rst.addRoute(originalSource, originalDestination, new Sigma(scp.getMinPaths(), scp.getMinPaths()));
        } else {
            ClosenessCentralityCD cccd = (ClosenessCentralityCD) node.getProtocol(cccdPid);
            /** In this case node is on at least 1 minimum path from originalSource to originalDestination */
            if (cccd.getDistance(originalDestination) + cccd.getDistance(originalDestination) == scp.getDistance()) {
                // mi segno che sono sul min path da originalSource a originalDestination
                Sigma s;
                if (rst.containsRoute(originalSource, originalDestination)) {
                    s = rst.getSigma(originalSource, originalDestination);
                } else {
                    s = new Sigma();
                    rst.addRoute(originalSource, originalDestination, s);
                }

                s.s1 = scp.getMinPaths();
            }

            forwardPong(node, originalSource, scp, pid);
        }
    }

    private void forwardPong(Node node,Node originalSource, StressCentralityPayload scp, int pid) {
        for (Node backForward : routing.getRoute(originalSource)) {
            sendPong(node, backForward, scp, pid);
        }
    }

    private void sendPing(Node source, Node[] destinations, Node originalSource, int distance, int minPaths, int pid) {
        for (Node destination : destinations) {
            if (destination.isUp())
                sendPing(source, destination, originalSource, distance, minPaths, pid);
        }
    }

    private void sendPing(Node source, Node destination, Node originalSource, int distance, int minPaths, int pid) {
        RequestMessage rMessage = new RequestMessage(source, destination, new StressCentralityPayload(originalSource, null, distance, minPaths));
        StressCentralityCD sscd = (StressCentralityCD) destination.getProtocol(pid);
        sscd.addRequestMessage(rMessage);
    }

    private void sendPong(Node source, Node destination, StressCentralityPayload scp, int pid) {
        ResponseMessage rMessage = new ResponseMessage(source, destination, scp);
        StressCentralityCD sscd = (StressCentralityCD) destination.getProtocol(pid);
        sscd.addReplyMessage(rMessage);
    }

    private void addRequestMessage(RequestMessage rMessage) {
        ArrayList<RequestMessage> queue;
        Node originalSource = ((StressCentralityPayload) rMessage.getPayload()).getOriginalSource();

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
            final int distanceFromSource = getDistanceFromSource(queue);

            /** - this phase is needed in to discard request messages with 'wrong' distance
             *    (it happens when A sends a message to C at cycle k, but C doesn't process the message at k.
             *    At cycle k+1 B sends a message to C with distance k+1. In this cycle C will process messages from
             *    A and B with distances k, k+1, only the first message needs to be processed) */
            queue.removeIf(rMessage -> {
                StressCentralityPayload scp = (StressCentralityPayload) rMessage.getPayload();
                return scp.getDistance() != distanceFromSource;
            });

            /** 1- phase, request messages collection, process of # min. paths from originalSource, min. distance from originalSource */
            for (RequestMessage rMessage : queue) {
                StressCentralityPayload scp = (StressCentralityPayload) rMessage.getPayload();
                minPathsFromSource += scp.getMinPaths();
                routing.addRoute(originalSource, rMessage.getSource());
            }

            /** 2- phase, response messages */
            for (RequestMessage rMessage : queue) {
                sendPong(node, rMessage.getSource(), new StressCentralityPayload(originalSource, node, distanceFromSource, minPathsFromSource), pid);
            }

            queue.clear();

            /** 3- phase, forwarding request messages to all neighbors */
            NeighborsProtocol neighbors = (NeighborsProtocol) node.getProtocol(FastConfig.getLinkable(pid));
            sendPing(node, neighbors.getAllExcept(originalSource), originalSource, distanceFromSource + 1, minPathsFromSource, pid);
        }
    }


    private void fillRouteSigmaTable(Node node) {
        for (Route r : rst.getRoutes()) {
            Sigma s = rst.getSigma(r);

            if (s.s2 == -1) {
                Sigma s1 = rst.getSigma(node, r.getSource());
                Sigma s2 = rst.getSigma(node, r.getDestination());

                if (s1 != null && s2 != null)
                    s.s2 = s1.s1 * s2.s1;
            }
        }

        if (rst.getSize() > 0 && rst.isFilled()) {
            this.stressCentrality = computeStressCentrality();
            this.betweennessCentrality = computeBetwennessCentrality();
        }
    }

    private int computeStressCentrality() {
        int sc = 0;

        for (Route r : rst.getRoutes()) {
            Sigma s = rst.getSigma(r);
            sc += s.s2;
        }

        return sc;
    }

    private double computeBetwennessCentrality() {
        double bc = 0;

        for (Route r : rst.getRoutes()) {
            Sigma s = rst.getSigma(r);
            bc += ((double) s.s2 / (double) s.s1);
        }

        return bc;
    }


    /**
     * Min distance over a Queue of messages
     */
    private int getDistanceFromSource(ArrayList<RequestMessage> queue) {
        int distanceFromSource = ((StressCentralityPayload) queue.get(0).getPayload()).getDistance();

        for (RequestMessage rMessage : queue) {
            StressCentralityPayload scp = (StressCentralityPayload) rMessage.getPayload();
            distanceFromSource = Math.min(distanceFromSource, scp.getDistance());
        }

        return distanceFromSource;
    }


    private void addReplyMessage(ResponseMessage rMessage) {
        vec2.add(rMessage);
    }

    public Object clone() {
        StressCentralityCD cced = (StressCentralityCD) super.clone();
        cced.setFirstValue(new ArrayList<>());
        cced.setSecondValue(new ArrayList<>());
        cced.inQueue = new HashMap<>();
        cced.rst = new RouteSigmaTable();
        cced.routing = new Routing();
        cced.firstCycle = true;
        return cced;
    }
}
