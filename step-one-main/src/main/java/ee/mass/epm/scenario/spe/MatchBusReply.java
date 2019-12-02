package ee.mass.epm.scenario.spe;

import applications.bpm.Util;
import core.Coord;
import core.SimClock;
import org.flowable.engine.delegate.DelegateExecution;
import org.flowable.engine.delegate.ExecutionListener;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

public class MatchBusReply implements ExecutionListener {
    private static final double BUS_NEXT_STOP_DISTANCE_THRESHOLD = 35.0;
    public static final String VAR_OPPORTUNISTIC_ATTEMPTS = "opportunisticAttempts";

    Logger log = LoggerFactory.getLogger(this.getClass());


    @Override
    public void notify(DelegateExecution execution) {
//        String endAddress = (String) execution.getVariable("endAddress");

        String endCoordString = (String) execution.getVariable("segmentEndCoord");

        Coord endCoord = Util.extractCoordFromString(endCoordString);
        Coord busNextStopCoord = Util.extractCoordFromString((String) execution.getVariable("nextStopCoord"));

        updateAttemptCountVariable(execution);
        if (busNextStopCoord == null){
            log.error(execution.getEventName());
            log.error("ExecutionID: " + execution.getId());
            log.error("Dumping Variables");
            execution.getTransientVariables().forEach((key, val) ->
                    log.error("K: " + key + ", V: " + val.toString()));

        }
        String localhost = execution.getVariable("localhost").toString();
        String busName = execution.getVariable("busName").toString();


        log.info(String.format("[%s] Fog:%s got reply from Bus %s, distance= %s", SimClock.getFormattedTime(1), localhost, busName, endCoord.distance(busNextStopCoord)));


        execution.setVariable("busMatches", endCoord.distance(busNextStopCoord) < BUS_NEXT_STOP_DISTANCE_THRESHOLD);


    }

    private void updateAttemptCountVariable(DelegateExecution execution) {
        int previousAttempts;
        if (execution.hasVariable(VAR_OPPORTUNISTIC_ATTEMPTS)){
            previousAttempts = execution.getVariable(VAR_OPPORTUNISTIC_ATTEMPTS, Integer.class);
        } else {
            previousAttempts = 0;
        }
        execution.setVariable(VAR_OPPORTUNISTIC_ATTEMPTS, previousAttempts+1);
    }
}
