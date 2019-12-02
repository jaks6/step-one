package ee.mass.epm.scenario.spe;

import applications.bpm.BpmEngineApplication;
import applications.bpm.Util;
import applications.spe.RoadMonitoringApp;
import core.Coord;
import core.DTNHost;
import org.flowable.engine.delegate.DelegateExecution;
import org.flowable.engine.delegate.JavaDelegate;

import java.util.HashMap;

public class DecideFogOrCloudTask implements JavaDelegate {

    public static final double MIN_DISTANCE_FROM_FOG_SERVER = 45.0;

    static double experimentCloudOverrideRatioCounter = 1;

    // TODO: get from var/expression?
    public static final int FOG_MAX_PROCESSES_THRESHOLD = 5;

    @Override
    public void execute(DelegateExecution execution) {

        String startCoordString = execution.getVariable("segmentStartCoord", String.class);
        String endCoordString = execution.getVariable("segmentEndCoord", String.class);

        Coord startCoord = Util.extractCoordFromString(startCoordString);
        Coord endCoord = Util.extractCoordFromString(endCoordString);


        // check if segment start & end has fog servers..
        DTNHost startFog = null;
        DTNHost endFog = null;

        startFog = RoadMonitoringApp.getClosestServerFromCache(startCoord);
        endFog = RoadMonitoringApp.getClosestServerFromCache(endCoord);

        boolean fogMode = false;


        if (startFog != null && endFog != null ){
            boolean distancesUnderTreshold =
                    startFog.getLocation().distance(startCoord) <= MIN_DISTANCE_FROM_FOG_SERVER &&
                            endFog.getLocation().distance(endCoord) <= MIN_DISTANCE_FROM_FOG_SERVER;
            final double endFogLoad = endFog.getComBus().getDouble(BpmEngineApplication.NO_OF_PROCESSES_RUNNING, Double.MAX_VALUE);
            fogMode = distancesUnderTreshold && (endFogLoad <= FOG_MAX_PROCESSES_THRESHOLD);
        }

        HashMap<String, Object> processVars = new HashMap<>();
        Object localhost = execution.getVariable("localhost");
        if (fogMode){
            processVars.put("requesterAddress",String.valueOf(startFog.getAddress()));
            processVars.put("videoAnalyserAddress",String.valueOf(endFog.getAddress()));
            processVars.put("operatingMode", "fog");
        } else {

            processVars.put("requesterAddress", localhost);
            processVars.put("videoAnalyserAddress", localhost);
            processVars.put("operatingMode", "cloud");

        }

        experimentOverrideDecision(execution, processVars, localhost);
        processVars.put("resultsAddress", localhost);
        processVars.put("resultsProcessInstanceId", execution.getProcessInstanceId());

        execution.setVariables(processVars);

    }

    private synchronized void experimentOverrideDecision(DelegateExecution execution, HashMap<String, Object> processVars, Object localhost) {
        if (execution.hasVariable("experimentCloudRatioOverride")){
            double overrideRatio = execution.getVariable("experimentCloudRatioOverride", Double.class);
            if (experimentCloudOverrideRatioCounter % (1/overrideRatio) < 1.0){
                processVars.put("requesterAddress", localhost);
                processVars.put("videoAnalyserAddress", localhost);
                processVars.put("operatingMode", "cloud");
            }
            experimentCloudOverrideRatioCounter++;
        }


    }
}
