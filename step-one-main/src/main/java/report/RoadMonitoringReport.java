package report;


import applications.bpm.BpmEngineApplication;
import core.Application;
import core.ApplicationListener;
import core.DTNHost;
import core.SimError;
import org.apache.commons.lang3.tuple.Pair;
import org.flowable.common.engine.api.delegate.event.FlowableEngineEvent;

import java.util.HashMap;

public class RoadMonitoringReport extends Report implements ApplicationListener {

    public static final String PROCESS_STARTED = "process_start";
    public static final String ROAD_PROCESS_STARTED = "road_process_start";
    public static final String ROAD_FOG_DECISION_COMPLETED = "road_decision_completed";
    public static final String PROCESS_COMPLETED = "process_completed";
    public static final String PROCESS_CANCELLED = "process_cancelled";



    HashMap<String, RoadMonitoringReport.ProcessEntry> instanceMap;

    public RoadMonitoringReport() {
        instanceMap = new HashMap<>();
    }

    public void gotEvent(String event, Object params, Application app,
                         DTNHost host) {
        // Check that the event is sent by correct application type
        if (!(app instanceof BpmEngineApplication)) return;


        if (params == null) return;

        Pair<Double, FlowableEngineEvent> timeAndNameAndInstance = (Pair<Double, FlowableEngineEvent>) params;

        Double time = timeAndNameAndInstance.getLeft();
        FlowableEngineEvent flowableEvent = timeAndNameAndInstance.getRight();
        String instanceId = flowableEvent.getProcessInstanceId() + "_" + host.getName();
        String processKey = flowableEvent.getProcessDefinitionId();
        RoadMonitoringReport.ProcessEntry entry = instanceMap.getOrDefault(instanceId, new RoadMonitoringReport.ProcessEntry(instanceId,  host.getName(),processKey));

        switch (event){
            case ROAD_FOG_DECISION_COMPLETED:

                entry.operatingMode = (String) ((BpmEngineApplication) app).getEngine().getProcessEngine().getRuntimeService()
                        .getVariable(flowableEvent.getExecutionId(), "operatingMode");

                break;
            case PROCESS_STARTED:
                entry.startTime = time;
                entry.status = "process_started";
                entry.masterProcess = (String) ((BpmEngineApplication) app).getEngine().getProcessEngine().getRuntimeService()
                        .getVariable(flowableEvent.getExecutionId(), "resultsProcessInstanceId");

                break;
            case PROCESS_COMPLETED:
//                final String operatingMode = (String) ((FlowableProcessC) flowableEvent).getVariables().getOrDefault("operatingMode", "undefined");

                entry.completeTime = time;
                entry.totalTime = entry.completeTime - entry.startTime;
//                entry.endEnergy = getEnergy(host);
                entry.status = "process_completed";
                break;
            case PROCESS_CANCELLED:
                if (entry.status.equals("process_completed")){
                    entry.status = "conflicted_cancel";
                }
                entry.cancelTime = time;
                entry.totalTime = entry.cancelTime - entry.startTime;
//                entry.endEnergy = getEnergy(host);
                entry.status = "process_cancelled";
                break;
        }
        instanceMap.put(instanceId, entry);
    }

    private double getEnergy(DTNHost host){
        Double value = (Double)host.getComBus().
                getProperty(routing.util.EnergyModel.ENERGY_VALUE_ID);
        if (value == null) {
            throw new SimError("Host " + host +
                    " is not using energy fog");
        }
        return value;
    }

    @Override
    public void done() {
//        write("Bpm stats for scenario " + getScenarioName() +
//                "\nsim_time: " + format(getSimTime()));
        String header = "proc_id;hostname;proc_key;master_process;opmode;proc_started;proc_completed;proc_cancelled;totaltime;finalstatus";
        write(header);


        instanceMap.forEach((instance, entry) -> {
            String csvLine = String.join(";",
                    entry.instanceId,
                    entry.host,
                    entry.processName,
                    String.valueOf( entry.masterProcess),
                    String.valueOf( entry.operatingMode),
                    String.valueOf( entry.startTime),
                    String.valueOf( entry.completeTime),
                    String.valueOf( entry.cancelTime),
                    String.valueOf( entry.totalTime),
//                    String.valueOf( entry.startEnergy),
//                    String.valueOf( entry.endEnergy),
                    entry.status
            );

            write(csvLine);


//            String statsText = "Processes started: " + hostStats.get(PROCESS_STARTED) +
//                    "\nProcesses completed: " + hostStats.get(PROCESS_COMPLETED)+
//
//                    "\nActivities started: " + hostStats.get(ACTIVITY_STARTED)+
//                    "\nActivities completed: " + hostStats.get(ACTIVITY_COMPLETED)+
//
//                    "\nMessages sent: " + hostStats.get(SENT_MESSAGE)+
//                    "\nMessages received: " + hostStats.get(RECEIVED_MESSAGE)+
//
//                    "\nSignals received: " + hostStats.get(RECEIVED_SIGNAL_NEARBY)
//
//                    ;
//
//            write(statsText);

        });


        super.done();
    }

    private class ProcessEntry  {
        public String operatingMode;
        public String masterProcess;
        private String processName;
        double startTime;
        double completeTime;
        double cancelTime;
        double totalTime;
        String instanceId;
        String host;
        public double endEnergy;
        public double startEnergy;
        String status;

        public ProcessEntry(String id, String hostName, String processName) {
            this.instanceId = id;
            this.processName = processName;
            this.host = hostName;
        }
    }
}
