package p2p.deccen.core.protocols;

import p2p.deccen.core.transport.Message;
import peersim.core.Protocol;

import java.util.ArrayList;

/**
 * Created by paolocifariello.
 */
public abstract class NetworkedProtocol implements Protocol {

    /** list of incoming messages (Request / Response) */
    public ArrayList<Message> incomingMessages = new ArrayList<>();
    /** list of outgoing messages */
    public ArrayList<Message> outgoingMessages = new ArrayList<>();

    public int sentMessages = 0;

    @Override
    public Object clone() {
        NetworkedProtocol np = null;

        try {
            np = (NetworkedProtocol) super.clone();
        } catch (CloneNotSupportedException var3) {
            ;
        }

        np.incomingMessages = new ArrayList<>();
        np.outgoingMessages = new ArrayList<>();
        return np;
    }

    protected void sendMessage(Message rMessage) {
        this.outgoingMessages.add(rMessage);
        sentMessages++;
    }
}
