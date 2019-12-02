package ee.mass.epm.scenario.spe;

import applications.bpm.BpmEngineApplication;
import core.*;
import ee.mass.epm.sim.message.EngineMessageContent;
import ee.mass.epm.sim.message.SimMessage;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

import static applications.bpm.BpmEngineApplication.*;

public class FogVideoServerApplication extends Application {

    boolean bootstrapFinished = false;
    Logger log = LoggerFactory.getLogger(this.getClass());


    /** Application ID */
    public static final String APP_ID = "ee.mass.FogVideoServerApp";
    private DTNHost mHost;

    public FogVideoServerApplication(Settings s) {
        super.setAppID(APP_ID);
    }

    private ConnectionListener mConnectionListener = new ConnectionListener() {

        @Override
        public void hostsConnected(DTNHost host1, DTNHost host2) {

            if (mHost != host1 && mHost != host2){ return; }// skip if this doesnt concern us
            DTNHost remoteHost = ( host1 == mHost ) ? host2 : host1;
            if (bootstrapFinished)
                sendStartProcessMessage(mHost, remoteHost);
        }

        @Override
        public void hostsDisconnected(DTNHost host1, DTNHost host2) { }
    };

    // Copy-constructor
    public FogVideoServerApplication(FogVideoServerApplication a) {
        super(a);

    }

    @Override
    public Message handle(Message msg, DTNHost host) {
        return msg;
    }

    @Override
    public void update(DTNHost host) {
        if (!bootstrapFinished) doBootstrap(host);
        // Check if new connections are up
//        host.getConnections().forEach(conn -> conn.);
    }

    private void doBootstrap(DTNHost host) {
        log.info("Bootstrapping app");
        mHost = host;
        log.info("Attaching listener");
        SimScenario.getInstance().addConnectionListener(mConnectionListener);
        bootstrapFinished = true;
    }

    @Override
    public Application replicate() {
        log.info("Replicating Video App");
        return new FogVideoServerApplication(this);
    }


    public void sendStartProcessMessage(DTNHost from, DTNHost to) {
        String id = "videoReq" + SimClock.getIntTime() + "-" +
                mHost.getAddress();
        Message msg = new Message(from, to, id, 10);
        msg.setAppID(BpmEngineApplication.APP_ID);
        msg.addProperty(MESSAGE_PROPERTY_OPERATION, OPERATION_PROCESS_MESSAGE);
        msg.addProperty(MESSAGE_PROPERTY_TYPE, MSG_TYPE_BPM);

        SimMessage bpmMessage = new SimMessage("Start Message", to.getAddress(), 10);

        EngineMessageContent messageContent = new EngineMessageContent();
        messageContent.isForStartEvent = true;
        DTNHost targetServer = getNextServerInLine(to);
        messageContent.variables.put("target_server", targetServer.getAddress());
        bpmMessage.name = "Start Message";

        msg.addProperty(PROPERTY_PROCESS_MSG, bpmMessage);
        mHost.createNewMessage(msg);

    }


    private DTNHost getNextServerInLine(DTNHost bus) {
        String targetServer = null;

        switch (mHost.getName()){
            case "server3":
                if (bus.getLocation().getX() > mHost.getLocation().getX()){
                    targetServer = "server4";
                } else {
                    targetServer = "server5";
                }
                break;
            case "server4":
                targetServer = "server3";
                break;
            case "server5":
                targetServer = "server3";
                break;
        }
        String finalTargetServer = targetServer;
        DTNHost targetHost = SimScenario.getInstance().getHosts()
                .stream()
                .filter(h -> h.getName().equals(finalTargetServer))
                .findFirst()
                .orElse(null);


        return targetHost;
    }

}
