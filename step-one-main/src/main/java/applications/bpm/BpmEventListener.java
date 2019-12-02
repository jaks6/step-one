package applications.bpm;

import core.Application;
import core.DTNHost;
import core.SimClock;
import org.apache.commons.lang3.tuple.ImmutablePair;
import org.flowable.common.engine.api.delegate.event.*;
import org.flowable.engine.delegate.event.FlowableActivityEvent;
import org.flowable.engine.delegate.event.FlowableCancelledEvent;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

import static report.BpmAppReporter.*;
import static report.RoadMonitoringReport.ROAD_FOG_DECISION_COMPLETED;
import static report.RoadMonitoringReport.ROAD_PROCESS_STARTED;


class BpmEventListener implements FlowableEventListener {
    Logger log = LoggerFactory.getLogger(this.getClass());

    void logEvent(DTNHost host, FlowableEngineEvent engineEvent){ logEvent(host,engineEvent,""); }
    void logEvent(DTNHost host, FlowableEngineEvent engineEvent, String msg){

        log.debug(String.format("%s[h:%s]\t[%s/%s]\t\t%s : %s",
                SimClock.getIntTime(), host.getName(), engineEvent.getProcessDefinitionId(), engineEvent.getProcessInstanceId(), engineEvent.getType().name(), msg));
    }

    private final Application application;
    private final DTNHost host;

    public BpmEventListener(DTNHost host, BpmEngineApplication bpmEngineApplication) {
        this.host = host;
        this.application = bpmEngineApplication;
    }

    @Override
    public void onEvent(FlowableEvent event) {
        FlowableEventType type = event.getType();
        FlowableEngineEvent engineEvent = (FlowableEngineEvent) event;

//        Pair<Double, String> pair = new ImmutablePair<>( SimClock.getTime(), engineEvent.getProcessInstanceId());

        ImmutablePair<Double, FlowableEngineEvent> pair = new ImmutablePair<>( SimClock.getTime(), engineEvent);
        if (type.equals(FlowableEngineEventType.JOB_EXECUTION_SUCCESS)) {

        } else if (type.equals(FlowableEngineEventType.PROCESS_STARTED)){
            logEvent(host, engineEvent);
            application.sendEventToListeners(PROCESS_STARTED, pair, host);

            host.getComBus().updateDouble(BpmEngineApplication.NO_OF_PROCESSES_RUNNING, 1);
            if (engineEvent.getProcessDefinitionId().equals("roadanalysisprocessV2")) {
                application.sendEventToListeners(ROAD_PROCESS_STARTED, event, host);
            }
        } else if (type.equals(FlowableEngineEventType.PROCESS_COMPLETED)){
            logEvent(host, engineEvent);
            host.getComBus().updateDouble(BpmEngineApplication.NO_OF_PROCESSES_RUNNING, -1);

            application.sendEventToListeners(PROCESS_COMPLETED, pair, host);
        } else if (type.equals(FlowableEngineEventType.PROCESS_CANCELLED)){
            host.getComBus().updateDouble(BpmEngineApplication.NO_OF_PROCESSES_RUNNING, -1);

            String cause = (String) ((FlowableCancelledEvent) event).getCause();
            if (cause.equals("cancelled"))
                application.sendEventToListeners(PROCESS_CANCELLED, pair, host);

        }



        else if (type.equals(FlowableEngineEventType.ACTIVITY_STARTED)){
            application.sendEventToListeners(ACTIVITY_STARTED, null, host);

            //TODO: try to share activityId between events for shorter code?
            String activityId = ((FlowableActivityEvent) event).getActivityId();
            logEvent(host, engineEvent, activityId);


        } else if (type.equals(FlowableEngineEventType.ACTIVITY_COMPLETED)){
            String activityId = ((FlowableActivityEvent) event).getActivityId();
            if (activityId.equals("taskDecideFog")) {
                application.sendEventToListeners(ROAD_FOG_DECISION_COMPLETED, pair, host);
            }


            logEvent(host, engineEvent, activityId);
            application.sendEventToListeners(ACTIVITY_COMPLETED, null, host);
        } else if (type.equals(FlowableEngineEventType.ACTIVITY_CANCELLED)){
            String activityId = ((FlowableActivityEvent) event).getActivityId();
            String executionId = ((FlowableActivityEvent) event).getExecutionId();

            ((BpmEngineApplication)application).engine.activityCancelled(executionId); //Todo: move this to a better place
            application.sendEventToListeners(ACTIVITY_CANCELLED, null, host);
            logEvent(host, engineEvent, activityId);

        }

        else if (type.equals(FlowableEngineEventType.TASK_CREATED)){
            application.sendEventToListeners(TASK_CREATED, null, host);
        } else if (type.equals(FlowableEngineEventType.TASK_COMPLETED)){
            application.sendEventToListeners(TASK_COMPLETED, null, host);
        }


        else if (type.equals(FlowableEngineEventType.ENTITY_CREATED)){
            application.sendEventToListeners(ENTITY_CREATED, null, host);
        }        else if (type.equals(FlowableEngineEventType.ENTITY_DELETED)){
            application.sendEventToListeners(ENTITY_DELETED, null, host);
        }

        //Not using engine message event, instead using simulator means for this
//        else if (type.equals(FlowableEngineEventType.ACTIVITY_MESSAGE_RECEIVED)) {
//            application.sendEventToListeners(RECEIVED_MESSAGE, null, host);
//        }
//        else {
//            System.out.println("Event received: " + event.getType());
//        }


    }

    @Override
    public boolean isFailOnException() {
        return false;
    }

    @Override
    public boolean isFireOnTransactionLifecycleEvent() {
        return false;
    }

    @Override
    public String getOnTransaction() {
        return null;
    }
}
