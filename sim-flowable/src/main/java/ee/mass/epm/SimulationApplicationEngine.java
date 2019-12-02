package ee.mass.epm;

import ee.mass.epm.sim.LocationSignalSubscription;
import ee.mass.epm.sim.message.SimMessage;
import org.flowable.common.engine.api.delegate.event.FlowableEventListener;
import org.flowable.engine.ProcessEngine;
import org.flowable.engine.runtime.ProcessInstance;

import java.io.InputStream;
import java.util.List;
import java.util.Map;
import java.util.Queue;
import java.util.Set;

/** Provides basic interaction with BPMN engine, mainly process
 *  deployment, starting and messaging.
 */
public interface SimulationApplicationEngine {

    void addEngineEventsListener(FlowableEventListener sil);

    ProcessInstance startProcessInstance(String processKey);

    ProcessInstance startProcessInstance(String processKey, Map<String, Object> processVariables);

    ProcessInstance startProcessInstanceWithMessage(SimMessage msg);


    /** Simulator fetches messages to send from here */
    Queue<SimMessage> getPendingOutgoingMessages();

    /** Movement-related signals are only sent to processes who have signal
     * intermediate catches with the LocationSignalSubscription ExecutionListener*/
    Set<LocationSignalSubscription> getLocationSignalSubscriptions();

    /** Get the requests for sending messages that use the adaptive fog model */
    Set<FogMessageRequest> getFogMessageRequests();


    void message(SimMessage msg);

    void message(String msgName, String executionId);

    void message(String msgName, String executionId, Map<String, Object> processVariables);

    void signal(String signalName);

    void signal(String signalName, Map<String, Object> processVariables);

    void signal(String signalName, String executionId);

    void signal(String signalName, String executionId, Map<String, Object> processVariables);

    /**
     * Notifies the original waiting task that the message was sent.
     */
    void notifyMessageTransferred(SimMessage m);

    public List<ProcessInstance> getRunningInstances(boolean includeVars);

    EngineStats getStats();

    /**
     *
     * @param name should end with .bpmn or .bpmn20.xml!
     * @param value
     */
    void deploy(String name, String value);

    /**
     *
     * @param name should end with .bpmn or .bpmn20.xml! should be unique
     * @param inputStream
     */
    void deploy(String name, InputStream inputStream);


    int getQueuedSimTasksSize();

    void cancelRunningInstances();

    void cancelRunningInstances(String reason);

    int update();

    int getHostAddress();
    void setHostAddress(int host);

    void activityCancelled(String executionId);

    ProcessEngine getProcessEngine();
}
