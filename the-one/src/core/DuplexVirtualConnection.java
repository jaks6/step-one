package core;

import interfaces.SharedBandwidthInterface;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import routing.MessageRouter;

public class DuplexVirtualConnection extends Connection {

    Logger log = LoggerFactory.getLogger(this.getClass());


    private int msgsize;
    private int msgsent;
    private int currentspeed = 0;
    private double lastUpdate = 0;
    private DuplexConnection masterConnection;

    /**
     * Creates a new connection between nodes and sets the connection
     * state to "up".
     *
     * @param fromNode      The node that initiated the connection
     * @param fromInterface The interface that initiated the connection
     * @param toNode        The node in the other side of the connection
     * @param toInterface   The interface in the other side of the connection
     */
    public DuplexVirtualConnection(DTNHost fromNode, NetworkInterface fromInterface, DTNHost toNode, NetworkInterface toInterface) {
        super(fromNode, fromInterface, toNode, toInterface);
        this.msgsent = 0;
        this.lastUpdate = SimClock.getTime();
    }

    @Override
    public int startTransfer(DTNHost from, Message m) {
        this.msgFromNode = from;
        Message newMessage = m.replicate();
        int retVal = getOtherNode(from).receiveMessage(newMessage, from);

        if (retVal == MessageRouter.RCV_OK) {
            this.msgOnFly = newMessage;
            this.msgsize = m.getSize();
            this.msgsent = 0;
        }

        return retVal;
    }

    public void finalizeTransfer() {
        assert this.msgOnFly != null : "Nothing to finalize in " + this;
        assert msgFromNode != null : "msgFromNode is not set";

        this.bytesTransferred += msgOnFly.getSize();
        masterConnection.bytesTransferred += msgOnFly.getSize();


        getOtherNode(msgFromNode).messageTransferred(this.msgOnFly.getId(),
                msgFromNode);

        this.fromInterface.disconnect(this, toInterface);
        fromInterface.connections.remove(this);
        clearMsgOnFly();


    }


    /**
     * Clears the message that is currently being transferred.
     * Calls to {@link #getMessage()} will return null after this.
     */
    protected void clearMsgOnFly() {
        masterConnection.clearDuplexTransfer(this.msgOnFly.getId());
        msgsent = 0;
        super.clearMsgOnFly();

        // Update transmit speed before next world update!
        ((SharedBandwidthInterface) this.fromInterface).updateTransmitSpeed();
//        ((SharedBandwidthInterface) this.toInterface).updateTransmitSpeed(); // one interface of 2 is enough
    }


    /**
     * Aborts the transfer of the currently transferred message.
     */
    public void abortTransfer() {
        assert msgOnFly != null : "No message to abort at " + msgFromNode;
        int bytesRemaining = getRemainingByteCount();

        this.bytesTransferred += msgOnFly.getSize() - bytesRemaining;
        masterConnection.bytesTransferred += msgOnFly.getSize();


        getOtherNode(msgFromNode).messageAborted(this.msgOnFly.getId(),
                msgFromNode, bytesRemaining);
        this.fromInterface.disconnect(this, toInterface);
        fromInterface.connections.remove(this);
        clearMsgOnFly();
    }


    /**
     * Returns true if the current message transfer is done.
     * @return True if the transfer is done, false if not
     */
    public boolean isMessageTransferred() {
        return msgsent >= msgsize;
    }

    @Override
    public double getSpeed() {
        return currentspeed;
    }

    public void setMasterConnection(DuplexConnection masterConnection) {
        this.masterConnection = masterConnection;
    }


    public void update() {
        if (msgOnFly == null) {
            log.warn("Skipping update since msgOnFly null!!");
            return;
        }
        double now = core.SimClock.getTime();
        int prevsent = msgsent;

        currentspeed = ((SharedBandwidthInterface) this.fromInterface).getUpSpeed();
        int downSpeed = ((SharedBandwidthInterface) this.toInterface).getDownSpeed();

        if (downSpeed < currentspeed) {
            currentspeed = downSpeed;
        }

        msgsent += currentspeed * (now - this.lastUpdate);


        log.debug(String.format("%1$14s  %2$6s , %3$6s  +%4$10s \t\t%5$10s /%6$10s", msgOnFly.getId(), this.fromInterface.host.getName(), SimClock.getTime(), msgsent-prevsent, msgsent, msgsize));
        //TODO: update bytes transferred?
        this.lastUpdate = now;

    }

    /**
     * Returns the amount of bytes to be transferred before ongoing transfer
     * is ready or 0 if there's no ongoing transfer or it has finished
     * already
     * @return the amount of bytes to be transferred
     */
    public int getRemainingByteCount() {
        int bytesLeft = msgsize - msgsent;
        return (bytesLeft > 0 ? bytesLeft : 0);
    }

    /**
     * Returns a String presentation of the connection.
     */
    public String toString() {
        return "(subConnection) " + fromNode + "<->" + toNode + " (" + getSpeed()/1000 + " kBps) is " +
                (isUp() ? "up":"down") +
                (isTransferring() ? " transferring " + this.msgOnFly  +
                        " from " + this.msgFromNode : "");
    }
}
