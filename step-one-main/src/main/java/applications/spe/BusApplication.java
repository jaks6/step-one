package applications.spe;

import applications.bpm.BpmEngineApplication;
import core.*;
import ee.mass.epm.sim.message.EngineMessageContent;
import ee.mass.epm.sim.message.SimMessage;
import movement.BusControlSystem;
import movement.BusMovement;
import movement.Path;
import org.apache.commons.lang3.StringUtils;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

import java.util.HashMap;
import java.util.List;
import java.util.Map;

import static applications.bpm.BpmEngineApplication.*;

public class BusApplication extends Application {

    Logger log = LoggerFactory.getLogger(this.getClass());

    public static final String APP_ID = "ee.mass.BusApp";
    private boolean cloudProcessStarted = false;
    Map<Coord, Integer> fogServersMap = new HashMap<>();
    private boolean bootstrapDone = false;
    private List<Coord> currentLineStops;

    public BusApplication(Settings s) {
        super.setAppID(APP_ID);



    }
    // Copy-constructor
    public BusApplication(BusApplication a) { super(a);    }

    @Override
    public Message handle(Message msg, DTNHost host) {

        if (msg.getTo().equals(host) ){
            SimMessage simMsg = (SimMessage) msg.getProperty(PROPERTY_PROCESS_MSG);

            if (simMsg.name.equals("Next Stop Inquiry")){
                handleStopInquiry(host, msg.getFrom(), simMsg);
            }


        }
        return msg;
    }

    private void handleStopInquiry(DTNHost host, DTNHost from, SimMessage simMsg) {
        Message m = new Message(host, from, "busStopReply" +
                SimClock.getIntTime() + "-" + host.getAddress() + "-to-"+simMsg.hashCode(),
                5);


        SimMessage msg = new SimMessage("Next Stop Reply", from.getAddress(), 5);
        EngineMessageContent messageContent = new EngineMessageContent();

//        Path hostPath = host.getPath();
        BusMovement movement = (BusMovement) host.getMovement();
        Coord nextStop = currentLineStops.get(movement.getRoute().getStopIndex());
//        if (hostPath == null){
//            log.error("Bus "  + host.getName() +" path was null! sending nextStop val: " + nextStop);
//        }
        messageContent.variables.put("nextStopCoord", nextStop.toString() );
        messageContent.variables.put("busName", host.getName());

//        if (hostPath != null){
//            Coord destination = hostPath.getCoords().get(hostPath.getCoords().size() - 1);
//
//
//
////            BusControlSystem busControlSystem = BusControlSystem.getBusControlSystem(1); // TODO: Hardcode!
//            int i = currentLineStops.indexOf(destination);
//
//
//            List<Coord> stopsLeft = currentLineStops.subList(i, currentLineStops.size() - 1);
////            List<String> nextStops = stopsLeft.stream()
////                    .map(coord -> fogServersMap.getOrDefault(coord, -1).toString())
////                    .collect(Collectors.toList());
//
//
//            Coord nextStop;
//            if (i == currentLineStops.size()-1){
//                nextStop = currentLineStops.get(0);
//            } else {
//                nextStop = currentLineStops.get(i + 1);
//            }
//
//
//            //TODO is above unnecessary ??
//            log.error("Bus: " + host.getName()+" sending nextStop " + nextStop +  "to: " + m.getTo().getName());
//
//            if (nextStop == null){
//                log.error("nextStop was null!! " + currentLineStops.toString() + " i="+i);
//                for (int j = 0; j < currentLineStops.size(); j++) {
//                    log.error(String.format("[j:%s], Stop: %s", j, currentLineStops.get(j)));
//                }
////                log.warn("overriding nextStop manually with 1st stop of line!! " + currentLineStops.get(0).toString());
////                nextStop = currentLineStops.get(0);
//            } else {
//                messageContent.variables.put("nextStopCoord", nextStop.toString() );
//                messageContent.variables.put("busName", host.getName());
////            messageContent.variables.put("nextStops", nextStops );
//            }
//
//        } else {
//            log.error("Bus host path was null!! " + host.getName());
//        }
        msg.setContent(messageContent);

        m.addProperty("operation", OPERATION_PROCESS_MESSAGE);
        m.addProperty("type", MSG_TYPE_BPM);
        m.addProperty(PROPERTY_PROCESS_MSG, msg);
        m.setAppID(BpmEngineApplication.APP_ID);

        log.debug("[BUS-" + host + "] Sent: "+ msg );


        host.createNewMessage(m);

    }

    @Override
    public void update(DTNHost host) {

        if (!bootstrapDone) {
            bootstrap(host);
        }
    }

    private void bootstrap(DTNHost host) {
        List<DTNHost> hosts = SimScenario.getInstance().getHosts();
//            DTNHost destinationFog = hosts.stream()
//                    .filter( h -> h.getLocation().equals(nextStop) &&
//                            h.getName().startsWith("fog"))
//                    .findFirst().get();
            // TODO: hardcode


        hosts.stream().filter(dtnHost -> dtnHost.getName().startsWith("fog")).forEach( h -> {
            fogServersMap.put(h.getLocation(), h.getAddress());
        } );


        int busLine = Integer.valueOf(StringUtils.substringBetween(host.getName(), "Bus", "-"));
        currentLineStops = BusControlSystem.getBusControlSystem(busLine).getBusStops();

        bootstrapDone = true;
    }


    @Override
    public Application replicate() {
        log.info("Replicating Bus App");
        return new BusApplication(this);
    }
}
