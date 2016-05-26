package p2p.deccen.core.controls;

import p2p.deccen.core.protocols.ClosenessCentralityCD;
import peersim.config.Configuration;
import peersim.core.Control;
import peersim.core.Network;
import peersim.core.Node;

import java.util.Random;

/**
 * Created by paolocifariello.
 */
public class InitRootNode implements Control {
    private static final String CC_PROTOCOL = "protocol";
    private static final String INIT_ALL = "initAll";

    private static int pid ;
    private static boolean initAll = false;

    public InitRootNode(String prefix){
        pid = Configuration.getPid(prefix + "." + CC_PROTOCOL) ;
        initAll = Configuration.getBoolean(prefix + "." + INIT_ALL);
    }

    /** return true if the simulation has to be stopped */
    public boolean execute () {
        if (initAll) {
            initAllNodes();
        } else {
            initRandomNode();
        }

        return false;
    }

    private void initAllNodes() {
        for (int i = 0; i < Network.size(); ++i) {
            setRoot(i);
        }
    }

    private void initRandomNode() {
        Random rand = new Random(System.nanoTime());
        setRoot(rand.nextInt(Network.size()));
    }

    private void setRoot(int id) {
        Node node = Network.get(id);
        ClosenessCentralityCD cced = (ClosenessCentralityCD) node.getProtocol(pid);
        cced.setRoot();
    }
}
