package p2p.deccen.core.controls;

import org.graphstream.algorithm.APSP;
import org.graphstream.algorithm.BetweennessCentrality;
import org.graphstream.graph.Graph;
import org.graphstream.graph.implementations.DefaultGraph;
import org.graphstream.stream.file.FileSource;
import org.graphstream.stream.file.FileSourceFactory;
import p2p.deccen.core.protocols.ClosenessCentralityCD;
import p2p.deccen.core.protocols.StressCentralityCD;
import peersim.config.Configuration;
import peersim.core.Control;
import peersim.core.Network;
import peersim.core.Node;

import java.io.IOException;

/**
 * Created by paolocifariello.
 */
public class CorrectnessCheck implements Control {

    private static final String FILE = "file";
    private static String file;

    private static final String CC_PROTOCOL = "ccProtocol";
    private static int cccdPid;
    private static final String SC_PROTOCOL = "scProtocol";
    private static int sccdPid;



    public CorrectnessCheck (String prefix) {
        file = Configuration.getString(prefix + "." + FILE);
        cccdPid = Configuration.getPid(prefix + "." + CC_PROTOCOL);
        sccdPid = Configuration.getPid(prefix + "." + SC_PROTOCOL);
    }


    public boolean execute() {

        Graph graph = new DefaultGraph("g");
        FileSource fs = null;

        try {
            fs = FileSourceFactory.sourceFor(file);
            fs.addSink(graph);
            fs.readAll(file);
        } catch (IOException e) {
            e.printStackTrace();
        } finally {
            fs.removeSink(graph);
        }

        APSP apsp = new APSP();
        apsp.init(graph); // registering apsp as a sink for the graph
        apsp.setDirected(false); // undirected graph
        apsp.compute();

        BetweennessCentrality bc = new BetweennessCentrality();
        bc.init(graph);

//        bc.compute();

        for (int i = 1; i < Network.size(); i++) {
            Node node = Network.get(i);

            APSP.APSPInfo info = graph.getNode(Integer.toString(i)).getAttribute(APSP.APSPInfo.ATTRIBUTE_NAME);

            if (!checkClosenessCentralityCorrectness(node, info)) {
                System.err.println("Error");
            }

//            Double bcValue = g.getNode(i).getAttribute("Cb");
            StressCentralityCD sccd = (StressCentralityCD) node.getProtocol(sccdPid);

            System.out.println("Expected " + 1 + ", but got " + sccd.betweennessCentrality);
        }

        return true;
    }

    private boolean checkClosenessCentralityCorrectness(Node node, APSP.APSPInfo info) {
        ClosenessCentralityCD cccd = (ClosenessCentralityCD) node.getProtocol(cccdPid);
        for (Node n : cccd.distances.keySet()) {
            int distance = cccd.getDistance(n);
            int expectedDistance = (int) info.getLengthTo(String.valueOf(n.getID()));

            if (distance != expectedDistance)
                return false;
        }

        return true;
    }
}
