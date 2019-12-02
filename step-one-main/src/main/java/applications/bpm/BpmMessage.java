package applications.bpm;

import core.DTNHost;
import core.Message;

public class BpmMessage extends Message {
    /**
     * Creates a new Message.
     *
     * @param from Who the message is (originally) from
     * @param to   Who the message is (originally) to
     * @param id   Message identifier (must be unique for message but
     *             will be the same for all replicates of the message)
     * @param size Size of the message (in bytes)
     */
    public BpmMessage(DTNHost from, DTNHost to, String id, int size) {
        super(from, to, id, size);
    }
}
