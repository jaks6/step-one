package applications.bpm;

import core.Coord;
import core.DTNHost;
import core.MovementListener;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

public class BpmMovementListener implements MovementListener {
    private final DTNHost mHost;
    private final BpmEngineApplication engine;

    Logger log = LoggerFactory.getLogger(this.getClass());


    public BpmMovementListener(BpmEngineApplication bpmEngineApplication) {
        this.engine = bpmEngineApplication;
        mHost = this.engine.getHost();
    }

    @Override
    public void newDestination(DTNHost host, Coord destination, double speed) {
        if (shouldNewProcessBeStarted(host)){

            // is it the beginning event of the path?
            if (destination.equals(host.getPath().getCoords().get(0))){
                log.debug("host = [" + host + "], destination = [" + destination + "] Starting new BP");
                startProcessInstance();
            }
        }
    }

    private boolean shouldNewProcessBeStarted(DTNHost host){
//        if (mHost.equals(host) && engine.isMobile()) {
//            return ((ActiveRouter) mHost.getRouter()).hasEnergy();
//        } return false;
        return false; //TODO
    }

    private void startProcessInstance() {
        //TODO: we should properly handle cancelled instances

        //clear old processes
        engine.cancelRunningInstances();

//        engine.startProcessInstance(MOBILE_PROCESS_KEY);
        engine.handleAutostartProcesses(); //TODO: it may be unclear that new paths also start processes!
    }

    @Override
    public void initialLocation(DTNHost host, Coord location) {
        log.debug(String.format(" [BPH-%s] Initial location: %s", host.getName(), location));
        //TODO: commented out because this gets called before bp engine is initalized
//        if (shouldNewProcessBeStarted(host)){
//            log.debug("host = [" + host + "], Starting new BP");
//            startProcessInstance();
//        }
    }
}
