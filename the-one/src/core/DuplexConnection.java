package core;

import interfaces.SharedBandwidthInterface;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import routing.MessageRouter;

import java.util.Collection;
import java.util.Iterator;
import java.util.concurrent.ConcurrentHashMap;

public class DuplexConnection extends Connection {

    Logger log = LoggerFactory.getLogger(this.getClass());

    private int msgsize;
    private int msgsent;
    private int currentspeed = 0;
    private double lastUpdate = 0;
    private DTNHost creator;



    ConcurrentHashMap<String, DuplexVirtualConnection> subConnections;


    DuplexConnection subConnection1;

//    HashSet<DuplexVirtualConnection> subConnections;


    /**
     * Creates a new connection between nodes and sets the connection
     * state to "up".
     *
     * @param fromNode      The node that initiated the connection
     * @param fromInterface The interface that initiated the connection
     * @param toNode        The node in the other side of the connection
     * @param toInterface   The interface in the other side of the connection
     */
    public DuplexConnection(DTNHost fromNode, NetworkInterface fromInterface, DTNHost toNode, NetworkInterface toInterface) {
        super(fromNode, fromInterface, toNode, toInterface);
//        subConnections = new HashSet<>();
        subConnections = new ConcurrentHashMap<>();
        msgOnFly = null;
    }

    /**
     * Sets a message that this connection is currently transferring. If message
     * passing is controlled by external events, this method is not needed
     * (but then e.g. {@link #finalizeTransfer()} and
     * {@link #isMessageTransferred()} will not work either). Only a one message
     * at a time can be transferred using one connection.
     * @param from The host sending the message
     * @param m The message
     * @return The value returned by
     * {@link MessageRouter#receiveMessage(Message, DTNHost)}
     */
    @Override
    public int startTransfer(DTNHost from, Message m) {

        if (subConnections.containsKey(m.getId())){
            return MessageRouter.DENIED_UNSPECIFIED;
        }
        DuplexVirtualConnection con;

        // determine and set the subconnection's direction
        if (m.getFrom().equals(fromNode)){
             con = new DuplexVirtualConnection(this.fromNode, this.fromInterface, toNode, toInterface);

        } else {
            con = new DuplexVirtualConnection(this.toNode, this.toInterface, fromNode, fromInterface);
        }
        con.setMasterConnection(this);



        int retVal = con.startTransfer(from, m);
        if (retVal == MessageRouter.RCV_OK){
            subConnections.put(m.getId(), con);


            // TODO: Ugly DRY
            if (m.getFrom().equals(fromNode)){
                this.fromInterface.connect(con, toInterface);
            } else {
                this.toInterface.connect(con, fromInterface);

            }
        }
        return retVal;
    }

    @Override
    public int getRemainingByteCount() {
        int total = 0;

        for (DuplexVirtualConnection subConnection : subConnections.values()) {
            total += subConnection.getRemainingByteCount();
        }
        return total;
    }

    @Override
    public boolean isMessageTransferred() {
        return false;
    }

    @Override
    public double getSpeed() {
        return currentspeed;
    }

    @Override
    public int getTotalBytesTransferred() {
        int total = this.bytesTransferred;

        for (DuplexVirtualConnection subConnection : subConnections.values()) {
            total += subConnection.getTotalBytesTransferred();
        }
        return total;
    }

    //    @Override
//    public double getSpeed() {
//        return 0;
//    }

    /** Returns the first message on fly */
    @Override
    public Message getMessage() {
        if (getTransfers().hasNext()){
            DuplexVirtualConnection virtualConnection = getTransfers().next();
            return virtualConnection.msgOnFly;
        }
        return null;
    }


    public Iterator<DuplexVirtualConnection> getTransfers() {
        return this.subConnections.values().iterator();
    }

    public Collection<DuplexVirtualConnection> getSubConnections(){
        return this.subConnections.values();
    }

    public int getSubConnectionSpeed(DuplexVirtualConnection duplexVirtualConnection) {
        return currentspeed;
    }

    /**
     * Calculate the current transmission speed from the information
     * given by the interfaces, and calculate the missing data amount.
     */
    public void update() {

        //log.debug(String.format("\t%1$6s [%2$6s]\tUpdating (%3$s) subConnections", SimClock.getTime(), fromNode+"-"+toNode, subConnections.size()));
        //TODO: find out how much bandwidth is left for this connecion.. or use round robin scheme?
        currentspeed =  this.fromInterface.getTransmitSpeed(toInterface);
        int othspeed =  this.toInterface.getTransmitSpeed(fromInterface);
        double now = core.SimClock.getTime();

        if (othspeed < currentspeed) {
            currentspeed = othspeed;
        }

        for (DuplexVirtualConnection subConnection : subConnections.values()) {
            subConnection.update();
        }

        this.lastUpdate = now;


    };

    public void clearDuplexTransfer(String msgId) {
        subConnections.remove(msgId);
    }

    /**
     * Returns a String presentation of the connection.
     */
    public String toString() {
        return fromNode + "<->" + toNode + " (" + getSpeed()/1000 + " kBps) is " +
                (isUp() ? "up":"down") +
                (isTransferring() ? " transferring " + subConnections.size()  +
                        " transfers from " + this.msgFromNode : "");
    }

}
