package applications.bpm;

import core.*;
import ee.mass.epm.sim.message.SimMessage;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

/** TODO: consider if there is too much overhead if every node
 *  has its own listener as it is currently implemented.
 *
 * Right now, every node is notified of every message transfer in the world.
 */
public class BpmMessageListener implements MessageListener {

    private static Logger log = LoggerFactory.getLogger(BpmMessageListener.class);


    private static BpmMessageListener instance;

    static {
        DTNSim.registerForReset(BpmMessageListener.class.getCanonicalName());
        reset();
    }

    public static BpmMessageListener getInstance() {
        if (instance == null) {
            instance = new BpmMessageListener();
            log.warn("Attaching BpmMessageListener to Simulation Scenario!");
            SimScenario.getInstance().addMessageListener(instance);
        }
        return instance;
    }

    public static void reset() {
        instance = null;
    }


    public BpmMessageListener() {    }

    /** notifySourceOfDelivery is assigned in bpmn using flowable:istriggerable */
    public void messageTransferred(Message msg, DTNHost from, DTNHost to, boolean firstDelivery) {
        SimMessage simMsg = (SimMessage) msg.getProperty(BpmEngineApplication.PROPERTY_PROCESS_MSG);

        if (simMsg!= null && simMsg.notifySourceOfDelivery){

            BpmEngineApplication.ofHost(from).getEngine().notifyMessageTransferred(simMsg);
            //from.deleteMessage(msg.getId(), false); //TODO: this may cause problems with routing/relaying
        }
    }

    // not used
    public void messageTransferStarted(Message m, DTNHost from, DTNHost to) {}
    public void messageDeleted(Message m, DTNHost where, boolean dropped) { }
    public void newMessage(Message m) { }
    public void messageTransferAborted(Message m, DTNHost from, DTNHost to) { }



}
