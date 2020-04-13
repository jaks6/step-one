package ee.mass.epm;

import ee.mass.epm.sim.*;
import ee.mass.epm.sim.message.EngineMessageContent;
import ee.mass.epm.sim.message.SimMessage;
import org.flowable.common.engine.api.delegate.event.FlowableEvent;
import org.flowable.common.engine.api.delegate.event.FlowableEventListener;
import org.flowable.common.engine.api.delegate.event.FlowableEventType;
import org.flowable.common.engine.impl.runtime.Clock;
import org.flowable.engine.*;
import org.flowable.engine.interceptor.*;
import org.flowable.engine.parse.BpmnParseHandler;
import org.flowable.engine.runtime.Execution;
import org.flowable.engine.runtime.ProcessInstance;
import org.flowable.engine.runtime.ProcessInstanceQuery;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

import java.io.InputStream;
import java.util.*;

public class Engine implements SimulationApplicationEngine {

    public static int appIdCounter = 0; // used as a way to force unique h2 database ids


    public static final String LOCALHOST_VAR = "localhost";
    private int hostAddress;
    private ProcessEngine processEngine;
    private TaskService taskService;
    RepositoryService repositoryService;

    private boolean timerEventProcessesDeployed = false;
    private SimulatedWorkQueue simulatedWorkQueue;
    private Queue<SimMessage> outgoingMessageQueue;
    private Set<FogMessageRequest> fogMessageRequests;
    private Set<LocationSignalSubscription> locationSignalSubscriptions;
    private Set<String> deployedResourceNamesCache;

    RuntimeService runtimeService;


    Logger log = LoggerFactory.getLogger(this.getClass());


    public Engine(Clock clock) {
        this.init(clock, null);
    }

    public Engine() {
        this.init(null, null);
    }

    public Engine(Clock instance, CpuConf cpuConf) {
        this.init(instance, cpuConf);
    }


    public void init(Clock clock, CpuConf cpuConf) {
        String uniqueId = this.hashCode()+String.valueOf(Engine.appIdCounter);

        List<BpmnParseHandler> postParseHandlerList = new ArrayList<>();
        postParseHandlerList.add(new OneTimerParse(this));
        postParseHandlerList.add(new OneStartEventTimerParse(this));
        //TODO: verify db conf (in-memory needed?)
        SimulatedProcessEngineConfiguration cfg = (SimulatedProcessEngineConfiguration) new SimulatedProcessEngineConfiguration(cpuConf)

                .setJdbcUrl("jdbc:h2:mem:flowable" + uniqueId + ";DB_CLOSE_DELAY=-1")
//                .setJdbcUsername("sa")
//                .setJdbcPassword("")
//                .setJdbcDriver("org.h2.Driver")
//                .setAsyncExecutorActivate(true)

                .setPostBpmnParseHandlers(postParseHandlerList)
                .setStartProcessInstanceInterceptor( new StepONEProcessStartInterceptor(this) )
                .setDatabaseSchemaUpdate(ProcessEngineConfiguration.DB_SCHEMA_UPDATE_TRUE);

        if (clock != null){
            cfg.setClock(clock);
        }

        //TODO: consider ProcessEngineConfiguration.createStandaloneInMemProcessEngineConfiguration().buildProcessEngine();

        processEngine = cfg.buildProcessEngine();
        Engine.appIdCounter++;

        repositoryService = processEngine.getRepositoryService();
        taskService = processEngine.getTaskService();
        runtimeService = processEngine.getRuntimeService();

        simulatedWorkQueue = cfg.simulatedWorkQueue;
        outgoingMessageQueue = cfg.getOutgoingMessages();
        fogMessageRequests = cfg.getFogMessageRequests();
        locationSignalSubscriptions = cfg.getLocationSignalSubscriptions();
        deployedResourceNamesCache = new LinkedHashSet<>();

    }

    @Override
    public Queue<SimMessage> getPendingOutgoingMessages() {
        return outgoingMessageQueue;
    }

    @Override
    public Set<LocationSignalSubscription> getLocationSignalSubscriptions() {
        return locationSignalSubscriptions;
    }

    @Override
    public Set<FogMessageRequest> getFogMessageRequests() { return fogMessageRequests; }

    @Override
    public int getHostAddress() {
        return hostAddress;
    }

    @Override
    public void setHostAddress(int host){
        this.hostAddress = host;
        ((SimulatedProcessEngineConfiguration) processEngine.getProcessEngineConfiguration()).setHostAddress(host);
    }

    @Override
    public ProcessInstance startProcessInstanceWithMessage(SimMessage msg) {
        Map<String, Object> processVariables = ((EngineMessageContent) msg.getContent()).variables;
        // processVariables.put(LOCALHOST_VAR, hostAddress); // replaced by startprocessInterceptor
        return runtimeService.startProcessInstanceByMessage(msg.name, processVariables);
    }

    @Override
    public ProcessInstance startProcessInstance(String processKey) {
        return this.startProcessInstance(processKey, new HashMap<>());
    }

    @Override
    public ProcessInstance startProcessInstance(String processKey, Map<String, Object> processVariables) {
        // processVariables.put(LOCALHOST_VAR, hostAddress); // replaced by startprocessInterceptor
        return runtimeService.startProcessInstanceByKey(processKey, processVariables);
    }

    public void event(String name){

        runtimeService.dispatchEvent(new FlowableEvent() {
            @Override
            public FlowableEventType getType() {
                return null;
            }
        });
    }

    @Override
    public void addEngineEventsListener(FlowableEventListener listener) {
        runtimeService.addEventListener(listener);
    }



    @Override
    public void message(String msgName, String executionId) {
        log.debug("received msgName = [" + msgName + "], destinationProcessInstanceId = [" + executionId + "]");
        runtimeService.messageEventReceived(msgName, executionId);
    }

    @Override
    public void message(String msgName, String executionId, Map<String, Object> processVariables) {
        log.debug("received msgName = [" + msgName + "], destinationProcessInstanceId = [" + executionId + "], processVariables = [" + processVariables + "]");
        if (doesExecutionExist(executionId))
            runtimeService.messageEventReceived(msgName, executionId, processVariables);
        else
            log.debug("A Message was dropped because the execution does not exist");
    }

    @Override
    public void message(SimMessage msg){
        EngineMessageContent msgContent = (EngineMessageContent) msg.getContent();

        List<Execution> executions;
        if (msgContent.destinationProcessInstanceId != null){
            executions = getMessageExecutionForProcess(msg.name, msgContent.destinationProcessInstanceId);
        } else {
            executions = getMessageListenerExecutionsByName(msg.name);
        }

        for (Execution execution : executions) {
            if (executions.isEmpty()){
                log.warn("A message was dropped because the specified destinationExecutionId does not have a corresponding message subscription!");
            } else {
                runtimeService.messageEventReceived(msg.name, execution.getId(), msgContent.variables);
            }
        }

//        if (doesExecutionExist(msgContent.destinationProcessInstanceId))
//            runtimeService.messageEventReceived(msg.name, msgContent.destinationProcessInstanceId, msgContent.variables);
//        else
//            log.debug("A Message was dropped because the execution does not exist");
    }

    private List<Execution>  getMessageListenerExecutionsByName(String name){
        return runtimeService.createExecutionQuery().messageEventSubscriptionName(name).list();
    }

    private List<Execution> getMessageExecutionForProcess(String name, String processId){
        return runtimeService.createExecutionQuery().messageEventSubscriptionName(name).rootProcessInstanceId(processId).list();
    }

    private boolean doesExecutionExist(String executionId){
        return runtimeService.createExecutionQuery().executionId(executionId).count() > 0;
    }

    @Override
    public void signal(String name) {
        runtimeService.signalEventReceived(name);
    }

    @Override
    public void signal(String name, String executionId) { runtimeService.signalEventReceived(name, executionId);  }

    @Override
    public void signal(String name, Map<String, Object> processVariables) {
        runtimeService.signalEventReceived(name, processVariables);
    }

    @Override
    public void signal(String name, String executionId, Map<String, Object> processVariables) {
        runtimeService.signalEventReceived(name, executionId, processVariables);
    }


    /** Used to notify the original process that a send message task was succesfully received at the other end*/
    @Override
    public void notifyMessageTransferred(SimMessage msg) {
        if (msg.srcExecutionId == null) {
            log.warn("Tried to notify message transferred but srcExecutionId was null!");
            return;
        }
        if (runtimeService.createExecutionQuery().executionId(msg.srcExecutionId).count() > 0)
            runtimeService.trigger(msg.srcExecutionId);
        else
            log.warn("A Message transfer notification was dropped because the execution no longer exists");
    }


    @Override
    public void deploy(String name, String value){
        repositoryService.createDeployment().addString(name, value).deploy();
    }

    @Override
    public void deploy(String name, InputStream inputStream){
        if (!deployedResourceNamesCache.contains(name)) {
            repositoryService.createDeployment().addInputStream(name, inputStream).deploy();
            deployedResourceNamesCache.add(name);
        } else {
            log.debug("Not deploying {}, since it was already deployed!", name);
        }
    }

    public EngineStats getStats(){
        return EngineStats.create(this);
    }

    @Override
    public int getQueuedSimTasksSize(){
        return simulatedWorkQueue.getTimeToFinishJobs();
    }

    public List<ProcessInstance> getRunningInstances(boolean includeVars){
        ProcessInstanceQuery query = runtimeService.createProcessInstanceQuery();
        if (includeVars) query = query.includeProcessVariables();
        return query.list();
    }

    @Override
    public void activityCancelled(String executionId) {
        simulatedWorkQueue.cancelJob(executionId);
    }

    @Override
    public ProcessEngine getProcessEngine() {
        return processEngine;
    }

    @Override
    public void cancelRunningInstances() {
        int count = 0;
        runtimeService.createProcessInstanceQuery().active().list()
                .forEach(i -> {
                    simulatedWorkQueue.removeJobs(i.getProcessInstanceId());
                    runtimeService.deleteProcessInstance(i.getProcessInstanceId(), "cancelled");
                });
    }

    @Override
    public void cancelRunningInstances(String reason) {
        int count = 0;
        runtimeService.createProcessInstanceQuery().active().list()
                .forEach(i -> {
                    simulatedWorkQueue.removeJobs(i.getProcessInstanceId());
                    runtimeService.deleteProcessInstance(i.getProcessInstanceId(), reason);
                });

    }

    /** Returns how much work was done during this update */
    @Override
    public int update() {

        if (timerEventProcessesDeployed) { //This is a very naive optimization to avoid too many DB queries for timerjobs
            ((SimulatedProcessEngineConfiguration) processEngine.getProcessEngineConfiguration()).doTimerUpdate();
        }

        return simulatedWorkQueue.doWork();
    }

    public TaskService getTaskService() { return taskService; }

    public RepositoryService getRepositoryService() { return repositoryService; }

    public void setTimerEventProcessesDeployed(boolean timerEventProcessesDeployed) {
        this.timerEventProcessesDeployed = timerEventProcessesDeployed;
    }

    public boolean getTimerEventProcessesDeployed() {
        return this.timerEventProcessesDeployed;
    }
}
