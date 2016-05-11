package p2p.deccen.core.controls;

import org.graphstream.algorithm.BetweennessCentrality;
import org.graphstream.graph.Graph;
import org.graphstream.graph.implementations.DefaultGraph;
import org.graphstream.stream.file.FileSource;
import org.graphstream.stream.file.FileSourceFactory;
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

    private static final String SC_PROTOCOL = "scProtocol";
    private static int sccdPid;


    public CorrectnessCheck (String prefix) {
        file = Configuration.getString(prefix + "." + FILE);
        sccdPid = Configuration.getPid(prefix + "." + SC_PROTOCOL);
    }


    public boolean execute() {

        Graph g = new DefaultGraph("g");
        FileSource fs = null;

        try {
            fs = FileSourceFactory.sourceFor(file);
            fs.addSink(g);
            fs.readAll(file);
        } catch (IOException e) {
            e.printStackTrace();
        } finally {
            fs.removeSink(g);
        }

        BetweennessCentrality bc = new BetweennessCentrality();
        bc.init(g);
//        bc.compute();

        for (int i = 0; i < Network.size(); i++) {
            Node n = Network.get(i);
//            Double bcValue = g.getNode(i).getAttribute("Cb");
            StressCentralityCD sccd = (StressCentralityCD) n.getProtocol(sccdPid);

            System.out.println("Expected " + 1 + ", but got " + sccd.betweennessCentrality);
        }

        return false;
    }
}
