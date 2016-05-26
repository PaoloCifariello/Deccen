package p2p.deccen.core.protocols;

import p2p.deccen.core.transport.Message;
import p2p.deccen.core.transport.RequestMessage;
import p2p.deccen.core.transport.ResponseMessage;
import p2p.deccen.core.transport.StressCentralityPayload;
import p2p.deccen.core.util.RouteSigmaTable;
import p2p.deccen.core.util.Routing;
import p2p.deccen.core.util.Sigma;
import peersim.cdsim.CDProtocol;
import peersim.config.Configuration;
import peersim.config.FastConfig;
import peersim.core.Node;

import java.util.ArrayList;
import java.util.HashMap;

/**
 * Created by paolocifariello.
 */
public class StressCentralityCD extends NetworkedProtocol
        implements CDProtocol {

    private static final String CC_PROTOCOL = "ccProtocol";
    private static int cccdPid;

    private boolean firstCycle = true;
    /** list of incoming REQUEST messages, hashed on "original source" */
    private HashMap<Node, ArrayList<RequestMessage>> inQueue = new HashMap<>();

    private RouteSigmaTable rst = new RouteSigmaTable();

    private Routing routing = new Routing();

    public int stressCentrality;
    public double betweennessCentrality;

    public StressCentralityCD(String prefix) {
        cccdPid = Configuration.getPid(prefix + "." + CC_PROTOCOL);
    }

    @Override
    public void nextCycle(Node node, int pid) {
        if (firstCycle) {
            NeighborsProtocol neighbors = (NeighborsProtocol) node.getProtocol(FastConfig.getLinkable(pid));
            sendPing(node, neighbors.getAll(), node, 1, 1, pid);
            this.firstCycle = false;
        }

        processIncomingMessages(node, pid);

        /** RouteSigmaTable is completed partially at each cycle */
        if (rst.fill(node)) {
            stressCentrality = rst.computeStressCentrality(node);
            betweennessCentrality = rst.computeBetwennessCentrality(node);
        }
    }

    private void processIncomingMessages(Node node, int pid) {
        for (Message message : incomingMessages) {
            if (message instanceof RequestMessage) {
                processRequestMessage((RequestMessage) message);
            } else {
                /** forward back response messages */
                processResponseMessage(node, (ResponseMessage) message, pid);
            }
        }

        /** process request messages for each original source */
        for (Node originalSource : inQueue.keySet()) {
            processMessagesFrom(originalSource, node, pid);
        }

        incomingMessages.clear();
    }

    private void processRequestMessage(RequestMessage message) {
        Node originalSource = ((StressCentralityPayload) message.getPayload()).getOriginalSource();
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

        queue.add(message);
        inQueue.put(originalSource, queue);
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
            if (cccd.getDistance(originalSource) + cccd.getDistance(originalDestination) == scp.getDistance()) {
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
            sendPong(node, backForward, scp);
        }
    }

    private void sendPing(Node source, Node[] destinations, Node originalSource, int distance, int minPaths, int pid) {
        for (Node destination : destinations) {
            if (destination.isUp())
                sendPing(source, destination, originalSource, distance, minPaths);
        }
    }

    private void sendPing(Node source, Node destination, Node originalSource, int distance, int minPaths) {
        RequestMessage rMessage = new RequestMessage(source, destination, new StressCentralityPayload(originalSource, null, distance, minPaths));
        sendMessage(rMessage);
    }

    private void sendPong(Node source, Node destination, StressCentralityPayload scp) {
        ResponseMessage rMessage = new ResponseMessage(source, destination, scp);
        sendMessage(rMessage);
    }

    private void processMessagesFrom(Node originalSource, Node node, int pid) {
        ArrayList<RequestMessage> queue = inQueue.get(originalSource);

        if (!queue.isEmpty()) {
            int minPathsFromSource = 0;
            final int distanceFromSource = getDistanceFromSource(queue);

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
                StressCentralityPayload scp = new StressCentralityPayload(originalSource, node, distanceFromSource, minPathsFromSource);
                sendPong(node, rMessage.getSource(), scp);
            }

            queue.clear();

            /** 3- phase, forwarding request messages to all neighbors */
            NeighborsProtocol neighbors = (NeighborsProtocol) node.getProtocol(FastConfig.getLinkable(pid));
            sendPing(node, neighbors.getAllExcept(originalSource), originalSource, distanceFromSource + 1, minPathsFromSource, pid);
        }
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

    public Object clone() {
        StressCentralityCD sccd = (StressCentralityCD) super.clone();
        sccd.inQueue = new HashMap<>();
        sccd.rst = new RouteSigmaTable();
        sccd.routing = new Routing();

        sccd.firstCycle = true;

        return sccd;
    }
}