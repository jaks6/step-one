package applications.bpm;

import core.*;
import ee.mass.epm.Engine;
import ee.mass.epm.SimulationApplicationEngine;
import ee.mass.epm.sim.CpuConf;
import ee.mass.epm.sim.LocationSignalSubscription;
import ee.mass.epm.sim.message.*;
import ee.mass.epm.stepone.gui.BpmInfo;
import org.flowable.engine.history.HistoricProcessInstance;
import org.flowable.engine.runtime.Execution;
import org.flowable.engine.runtime.ProcessInstance;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import report.BpmAppReporter;
import routing.ActiveRouter;
import routing.util.EnergyModel;

import java.io.ByteArrayInputStream;
import java.io.IOException;
import java.io.InputStream;
import java.nio.file.Files;
import java.nio.file.Paths;
import java.util.HashMap;
import java.util.Iterator;
import java.util.List;
import java.util.Map;

public class BpmEngineApplication extends Application  {

    public static final int LOCATION_SIGNAL_DISTANCE_THRESHOLD = 15;

    public static final String PROCESSVAR_DELIMITER = ":";
    public static final String MESSAGE_PROPERTY_OPERATION = "operation";
    public static final String NO_OF_PROCESSES_RUNNING = "no_of_processes";
    public static final String COMBUS_RUNNING_PROCESSES = "no_of_processes";
    private static final int ENERGY_MODIFIER = 1;
    public static final String MESSAGE_PROPERTY_TYPE = "type";
    private static final String SETTING_TRACK_MOVEMENT = "generateMovementSignals";
    private static final String SETTING_TRACK_CONNECTIONS = "generateConnectionSignals";
    private static final String SETTING_NO_OF_CPUS = "noOfCpus";
    private static final String SETTING_CPU_SPEED = "cpuSpeed";
    private HashMap<String, Object>[] AUTOSTARTED_PROCESS_VARS;
    private String[] AUTO_STARTED_PROCESSES  = new String[]{};
    private String[] AUTO_DEPLOYED_PROCESSES = new String[]{};

    private static final String SETTING_AUTOSTARTED_PROCESSES = "autoStartedProcessKeys";

    // Should be in the form var1=var1Value&var2=var2Value ... etc (html url params - like)
    private static final String SETTING_AUTOSTARTED_PROCESSES_VARS = "autoStartedProcessVars";
    private static final String SETTING_AUTODEPLOYED_PROCESSES = "autoDeployedProcesses";




    /** Application ID */
    public static final String APP_ID = "ee.ut.cs.mc.BpmApplication";
    private static final int OPERATION_DEPLOY = 10;
    private static final int OPERATION_START_PROCESS_BY_KEY = 11;
    public static final int OPERATION_PROCESS_MESSAGE = 12;

    public static final String PROPERTY_PROCESS_MSG = "process_message";
    public static final String SIGNAL_NEARBY_DEVICE = "Device Nearby";
    public static final String SIGNAL_DISCONNECTED = "Disconnected";
    public static final String SIGNAL_NEW_DESTINATION = "New Destination";
    public static final String SIGNAL_LOCATION_UPDATE = "New Coordinate";

    public static final String MSG_TYPE_BPM = "bpm_msg";

    private static final String SETTING_OVERRIDE_MSG_SIZE = "overrideMsgSize";

    public static int OVERRIDE_MSG_SIZE = -1;

    Logger log = LoggerFactory.getLogger(this.getClass());

    private CpuConf cpuConf;
    SimulationApplicationEngine engine;
    boolean firstUpdate = true;
    DTNHost mHost;
    private boolean energyTrackingEnabled;

    private boolean movementTrackingEnabled;
    private boolean connectionSignalsEnabled = true;
    private boolean movementActiveOnLastUpdate = false;

    @Override
    public Message handle(Message msg, DTNHost host) {
        log.debug("Handling Message");
        String type = (String) msg.getProperty(MESSAGE_PROPERTY_TYPE);
        if (type != null && type.equals(MSG_TYPE_BPM)) {
            return handleBpmMessage(msg, host);
        } else {
            return msg;
        }
    }

    private Message handleBpmMessage(Message msg, DTNHost host) {
        if (msg.getTo() != host) return null; // are we the recipient?

        int operation = (Integer) msg.getProperty(MESSAGE_PROPERTY_OPERATION);
        switch (operation){
            case OPERATION_DEPLOY:
                handleOperationDeploy(msg);
                break;
            case OPERATION_START_PROCESS_BY_KEY:
                handleOperationStartProcessByKey(msg);
                break;
            case OPERATION_PROCESS_MESSAGE:
                super.sendEventToListeners(BpmAppReporter.RECEIVED_MESSAGE, null, host);
                handleOperationMessage(msg);
                break;
        }


        return null; //dont forward

    }

    private void handleOperationMessage(Message msg) {
        SimMessage simMsg = (SimMessage) msg.getProperty(PROPERTY_PROCESS_MSG);

        log.debug("[BPH-"+ mHost + "] Received : "+ simMsg);


        switch (simMsg.getContent().getMessageContentType()){
            case ENGINE_MSG:
                EngineMessageContent eMsgContent = (EngineMessageContent) simMsg.getContent();
                eMsgContent.variables.put("last_msg_source_address", msg.getFrom().getAddress());
                if (eMsgContent.isForStartEvent){
                    eMsgContent.variables.put("startMessageSenderAddress", msg.getFrom().getAddress());
                    engine.startProcessInstanceWithMessage(simMsg);
                } else {
                    engine.message(simMsg);
                }
                break;

            case DEPLOY_MSG:
                DeployMessageContent pMsgContent  = (DeployMessageContent) simMsg.getContent();
                InputStream inputStream = null;

                try {
                    inputStream = Files.newInputStream(Paths.get(pMsgContent.resourcePath));
                } catch (IOException e) {
                    log.error(e.getMessage());
                }
                engine.deploy(simMsg.name+".bpmn20.xml", inputStream);
                break;
        }
    }

    private void handleOperationStartProcessByKey(Message msg) {
        startProcessInstance((String) msg.getProperty("processKey"));
    }

    private void handleOperationDeploy(Message msg) {
        String name = (String) msg.getProperty("resourceName");
        byte[] bytes = (byte[]) msg.getProperty("bytes");
        engine.deploy(name, new ByteArrayInputStream(bytes));
    }

    public void handleSignal(String signalName, HashMap<String, Object> processVars) {
        log.debug("[BPH-{}] Signal: {} ( {} )", mHost, signalName, processVars.toString());

        engine.signal(signalName, processVars);
        sendEventToListeners("signalNearbyDevice", null, mHost);
    }


    /** This constructor defines the prototype Engine instance,
     *  based on which copies are made with  the copy constructor
     * @param s
     */
    public BpmEngineApplication(Settings s) {
        log.info("BpmApp Proto");


        if (s.contains(SETTING_OVERRIDE_MSG_SIZE)){
            OVERRIDE_MSG_SIZE = s.getInt(SETTING_OVERRIDE_MSG_SIZE);
        }

        movementTrackingEnabled = s.getBoolean(SETTING_TRACK_MOVEMENT, true);
        connectionSignalsEnabled = s.getBoolean(SETTING_TRACK_CONNECTIONS, true);

        if (s.contains(SETTING_NO_OF_CPUS) && s.contains(SETTING_CPU_SPEED)){
            this.cpuConf = new CpuConf(
                    s.getInt(SETTING_CPU_SPEED),
                    s.getInt(SETTING_NO_OF_CPUS));
        } else {
            log.info("Using default CPU configuration, as both no. of cpus and cpu_speed were not specified!");
            this.cpuConf = new CpuConf();
        }

        if (s.contains(SETTING_CPU_COST_PER_TIMEUNIT) || s.contains(SETTING_CPU_COST_TIMEUNIT) ){
            if ( s.contains(SETTING_CPU_COST_PER_TIMEUNIT) && s.contains(SETTING_CPU_COST_TIMEUNIT)) {
                costHelper.setCpuCost(s.getSetting(SETTING_CPU_COST_TIMEUNIT), s.getDouble(SETTING_CPU_COST_PER_TIMEUNIT));
            } else {
                log.error("Only one of cpuCostPerTimeUnit and cpuCostTimeUnit must be specified! Both must be specified.");
            }
        }



        if (s.contains(SETTING_BANDWIDTH_COSTS_PER_MB)){
            costHelper.setCpuCost(s.getCsvDoubles(SETTING_BANDWIDTH_COSTS_PER_MB));
        }

        if (s.contains(SETTING_AUTODEPLOYED_PROCESSES))
            AUTO_DEPLOYED_PROCESSES = s.getCsvSetting(SETTING_AUTODEPLOYED_PROCESSES);
        if (s.contains(SETTING_AUTOSTARTED_PROCESSES)){
            AUTO_STARTED_PROCESSES = s.getCsvSetting(SETTING_AUTOSTARTED_PROCESSES);
            AUTOSTARTED_PROCESS_VARS = new HashMap[AUTO_STARTED_PROCESSES.length];
        }
        if (s.contains(SETTING_AUTOSTARTED_PROCESSES_VARS)){

            String[] processes = s.getCsvSetting(SETTING_AUTOSTARTED_PROCESSES_VARS);
            for (int i = 0; i < processes.length; i++) {
                String[] processVars = processes[i].split("&");
                HashMap<String, Object>  varMap = new HashMap<>();

                for (String processVar : processVars) {

                    String[] split = processVar.split("=");
                    String varName = split[0].trim();
                    String varValue = s.valueFillString(split[1].trim());
                    varMap.put(varName, varValue);
                    AUTOSTARTED_PROCESS_VARS[i] = varMap;
                }
            }
        }


        super.setAppID(APP_ID);
    }

    /**
     * Copy-constructor: this is the main used constructor which initializes the engine
     * for each node in the simulation. Although this is a copy constructor, each copy
     * creates a new engine instance.
     *
     * @param e
     */
    public BpmEngineApplication(BpmEngineApplication e){
        super(e);
        this.AUTO_DEPLOYED_PROCESSES = e.AUTO_DEPLOYED_PROCESSES;
        this.AUTO_STARTED_PROCESSES = e.AUTO_STARTED_PROCESSES;
        this.AUTOSTARTED_PROCESS_VARS = e.AUTOSTARTED_PROCESS_VARS;
        this.movementTrackingEnabled = e.movementTrackingEnabled;
        this.engine = new Engine( OneSimClock.getInstance(), e.cpuConf);
    }



    @Override
    public void update(DTNHost host) {
        if (firstUpdate){ bootstrap(host); }
        else { //TODO: this means that autostarted processes and their tasks are ignored before bootstrap has been finished

            if (movementTrackingEnabled){
                updateMovement(host);
            }
            //make engine work on running tasks
            int workDone = engine.update();

            if (energyTrackingEnabled){
                updateEnergy(host, workDone);
            }
        }

        //take some messages and send them out
        handleOutgoingMessages(host);
    }
    // Convert movement updates to signals, if any processes have subscribed via MovementSignalSubscriptionTask
    private void updateMovement(DTNHost host) {
        // TODO, only bother with updates if there are any process definitions with subscriptions to movement.. ?
        if (movementActiveOnLastUpdate ){


            for (Iterator<LocationSignalSubscription> i = engine.getLocationSignalSubscriptions().iterator(); i.hasNext();) {
                LocationSignalSubscription subscription = i.next();

                if (subscription.getCoordinate().distance(host.getLocation()) <= LOCATION_SIGNAL_DISTANCE_THRESHOLD) {
                    engine.signal(SIGNAL_LOCATION_UPDATE, subscription.getExecutionId());
                    i.remove();
                }
            }
        }

        movementActiveOnLastUpdate = host.getMovement().isActive();
    }

    private void updateEnergy(DTNHost host, int workDone) {
        if (workDone > 0){
            if (host.getComBus().containsProperty(EnergyModel.ENERGY_VALUE_ID)){
                host.getComBus().updateDouble(EnergyModel.ENERGY_VALUE_ID, -workDone * ENERGY_MODIFIER);
            } else {
                log.warn("Missed workDone Energy update!!");
            }
        }
    }

    void bootstrap(DTNHost host) {
        firstUpdate = false;
        mHost = host;

        //initialize the singleton connection listener, so that bpm engines can get signals about new connections
        if (connectionSignalsEnabled){
            NewConnectionListener.getInstance();
        }
        BpmMessageListener.getInstance();

        host.getComBus().addProperty(NO_OF_PROCESSES_RUNNING, 0.0);

        movementActiveOnLastUpdate = host.isMovementActive();
        energyTrackingEnabled = ((ActiveRouter) host.getRouter()).getEnergy() != null;

        SimScenario.getInstance().addMovementListener(new BpmMovementListener(this));
        this.engine.addEngineEventsListener(new BpmEventListener(mHost, this));

        engine.setHostAddress(host.getAddress());
        deployProcesses(host);

        handleAutostartProcesses();


        log.info("[BPH-" + host + "] initialized engine: "+ this.engine);
    }

    public void handleAutostartProcesses() {
        for (int i = 0; i < AUTO_STARTED_PROCESSES.length; i++) {
            HashMap<String, Object> processVariables = AUTOSTARTED_PROCESS_VARS[i];
            if (processVariables != null){
                engine.startProcessInstance(AUTO_STARTED_PROCESSES[i], processVariables);
            } else {
                engine.startProcessInstance(AUTO_STARTED_PROCESSES[i]);
            }
        }
    }

    private void deployProcesses(DTNHost host) {
        // TODO avoid re-parsing XML each time, parse once, get BpmnModel Java object and deploy that
        try {
            for (String processFile : AUTO_DEPLOYED_PROCESSES) {
                engine.deploy(processFile, Util.getCachedProcess(processFile));
            }
        } catch (IOException e) {
            e.printStackTrace();
        }
    }

    private void handleOutgoingMessages(DTNHost host) {
        for (Iterator<SimMessage> i = engine.getPendingOutgoingMessages().iterator(); i.hasNext();) {

            SimMessage simMessage = i.next();

            Message m = messageFromBpm(host, simMessage);

            if (simMessage.destinationAddress == host.getAddress()){
                // Special case where the processes within one hosts enigne are exchanging messages
                //directly notify the router so that ONE reports can also react to the message
                host.getRouter().receiveMessage(m, host);
                host.getRouter().messageTransferred(m.getId(), host);
                //engine.notifyMessageTransferred(simMessage);
                //handleBpmMessage(m, host);
            } else {
                host.createNewMessage(m);
            }
            i.remove();
            // Call listeners
            this.sendEventToListeners(BpmAppReporter.SENT_MESSAGE, null, host);
        }
    }

    private Message messageFromBpm(DTNHost host, SimMessage simMessage) {
        DTNHost target =  SimScenario.getInstance().getWorld().getNodeByAddress(simMessage.destinationAddress);

        int size = (OVERRIDE_MSG_SIZE != -1) ? OVERRIDE_MSG_SIZE : simMessage.size;

        Message m = new Message(host, target, simMessage.name +
                SimClock.getIntTime() + "-" + host.getAddress() + "-" +
                target.getAddress() + "_" + simMessage.hashCode(),
                size);

        m.addProperty("operation", OPERATION_PROCESS_MESSAGE);
        m.addProperty(PROPERTY_PROCESS_MSG, simMessage);

        switch (simMessage.getContent().getMessageContentType()){
            case ONE_MSG:
                OneAppMessageContent content = (OneAppMessageContent) simMessage.getContent();
                m.setAppID(content.appId);
                break;
            case DEPLOY_MSG:
            case ENGINE_MSG:
                m.addProperty("type", MSG_TYPE_BPM);
                m.setAppID(APP_ID);
                break;
        }



        log.debug("[BPH-" + host + "] Sent: "+ simMessage );
        return m;
    }

    public void startProcessInstance(String processKey){
        this.engine.startProcessInstance(processKey);
//        super.sendEventToListeners(BpmAppReporter., null, mHost);
    }



    public int getSimulatedTaskQueueSize() {
        return engine.getQueuedSimTasksSize();
    }


    public void cancelRunningInstances() {
        engine.cancelRunningInstances();
    }
    public void cancelRunningInstances(String reason) {
        engine.cancelRunningInstances(reason);
    }

    @Override
    public Application replicate() {
        return new BpmEngineApplication(this);
    }


    public DTNHost getHost() {
        return mHost;
    }


    public SimulationApplicationEngine getEngine() {
        return engine;
    }

    /** Show BPM Info in GUI Details*/
    public BpmInfo getBpmInfo() {

        BpmInfo bpmInfo = new BpmInfo(this);

        List<ProcessInstance> instances = engine.getRunningInstances(true);
        BpmInfo processesInfo = new BpmInfo(instances.size() + " running process instances");

        // Add historic process instances
        List<HistoricProcessInstance> finishedInstances = engine.getProcessEngine().getHistoryService()
                .createHistoricProcessInstanceQuery().includeProcessVariables().finished().list();
        BpmInfo historyInfo = new BpmInfo(finishedInstances.size() + " finished processes");

        for (HistoricProcessInstance instance : finishedInstances) {
            BpmInfo historicInstanceInfo = new BpmInfo(instance.getId() + "-" + instance.getProcessDefinitionName());
            historicInstanceInfo.addMoreInfo((new BpmInfo("Started: " + (instance.getStartTime().getTime() / 1000))));
            historicInstanceInfo.addMoreInfo((new BpmInfo("Finished: " + (instance.getEndTime().getTime() / 1000))));

            // add process vars
            addInfoVariables(historyInfo, historicInstanceInfo, instance.getProcessVariables());
        }
        bpmInfo.addMoreInfo(historyInfo);

        // Running instances
        for (ProcessInstance instance : instances) {

            BpmInfo instanceInfo = new BpmInfo(instance.getProcessInstanceId() + "-" +  instance.getProcessDefinitionName());
            instanceInfo.addMoreInfo(new BpmInfo("Started at: " + (instance.getStartTime().getTime() / 1000)));

            // Add child executions
            List<Execution> executions = engine.getProcessEngine().getRuntimeService().createExecutionQuery().processInstanceId(instance.getProcessInstanceId()).onlyChildExecutions().list();

            BpmInfo executionsInfo = new BpmInfo(executions.size() + " child executions");
            for (Execution execution : executions) {
                executionsInfo.addMoreInfo( new BpmInfo("Execution:" + execution.getActivityId() ));
            }
            instanceInfo.addMoreInfo(executionsInfo);

            // add process vars
            addInfoVariables(processesInfo, instanceInfo, instance.getProcessVariables());
        }

        bpmInfo.addMoreInfo(processesInfo);

        return bpmInfo;
    }

    private void addInfoVariables(BpmInfo processesInfo, BpmInfo instanceInfo, Map<String, Object> processVariables) {
        BpmInfo variableInfo = new BpmInfo(processVariables.size() + " process variables");
        processVariables.forEach((key, val) -> variableInfo.addMoreInfo(new BpmInfo(key + ": " + val.toString())));
        instanceInfo.addMoreInfo(variableInfo);

        processesInfo.addMoreInfo( instanceInfo);
    }

    public boolean connectionSignalsEnabled() {
        return connectionSignalsEnabled;
    }
}


