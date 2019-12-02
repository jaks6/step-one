package applications.bpm;

import core.*;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

import java.util.HashMap;

public class NewDestinationSignalingListener implements MovementListener {

    private static NewDestinationSignalingListener instance;
    static Logger log = LoggerFactory.getLogger(NewDestinationSignalingListener.class);


    static {
        DTNSim.registerForReset(NewDestinationSignalingListener.class.getCanonicalName());
        reset();
    }

    public static void reset() {
        instance = null;
    }

    public static NewDestinationSignalingListener getInstance() {
        if (instance == null) {
            instance = new NewDestinationSignalingListener();
            log.debug("Attaching NewDestinationSignalingListener to Simulation Scenario!");
            SimScenario.getInstance().addMovementListener(instance);
        }
        return instance;
    }

    public NewDestinationSignalingListener() {
    }

    @Override
    public void newDestination(DTNHost host, Coord destination, double speed) {

        HashMap<String, Object> processVars = new HashMap<String, Object>();

        // Send Signal to host
        processVars.put("current_coordinates", host.getLocation().toString());
        processVars.put("destination_coordinates", host.getLocation().toString());
        signalDestination(host, BpmEngineApplication.SIGNAL_NEW_DESTINATION, processVars);

    }

    @Override
    public void initialLocation(DTNHost host, Coord location) {
        // TODO

    }

    private void signalDestination(DTNHost host1, String signal, HashMap<String, Object> processVars) {
        host1.getRouter().getApplications(BpmEngineApplication.APP_ID).forEach(
                a -> ((BpmEngineApplication)a).handleSignal(signal, processVars));
    }
}
