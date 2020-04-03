package applications.spe;

import applications.bpm.BpmEngineApplication;
import ee.mass.epm.scenario.spe.RoadSegmentAnalysis;
import ee.mass.epm.scenario.spe.Util;
import ee.mass.epm.scenario.spe.roadmonitoring.RoadSegment;
import core.*;
import ee.mass.epm.SimulationApplicationEngine;
import gui.playfield.NodeGraphic;
import movement.BusControlSystem;
import org.apache.commons.lang3.tuple.ImmutablePair;
import org.flowable.common.engine.api.delegate.event.FlowableEngineEvent;
import org.flowable.engine.ProcessEngine;
import org.flowable.engine.runtime.Execution;
import org.flowable.engine.runtime.ProcessInstance;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

import java.awt.*;
import java.util.List;
import java.util.*;
import java.util.concurrent.ConcurrentHashMap;
import java.util.stream.Collectors;

import static gui.playfield.PlayFieldGraphic.scale;
import static report.BpmAppReporter.PROCESS_COMPLETED;

public class RoadMonitoringApp extends Application implements ApplicationListener {



    public static final String ROADMONITOR_SETTING_SIMULTANEOUS_REQS = "simultaneousRequests";
    public static final String ROADMONITOR_SETTING_INTERVAL = "analysisInterval";
    public static final String ROADMONITOR_SETTING_OVERRIDE_CLOUDFOG_RATIO = "manualCloudFogRatioOverride";

    public static final int RATIO_NO_OVERRIDE = -1;


    public static int SIMULTANEOUS_SEGMENT_REQS = 3;
    public static double ANALYSIS_INTERVAL = 500;
    public static double ANALYSIS_ASSIGNMENT_CLOUDFOG_RATIO = RATIO_NO_OVERRIDE;

    Logger log = LoggerFactory.getLogger(this.getClass());

    public static final String APP_ID = "ee.mass.RoadMonitoringApp";

    private boolean bootstrapFinished = false;
    private BpmEngineApplication bpmEngineApp;

    private int analysisCounter = 0;

    private static Set<RoadSegment> roadSegmentRepository = new TreeSet<>();
    private static Set<DTNHost> fogServerRepository = new TreeSet<>();
    private static HashMap<Coord, DTNHost> closestServerCache = new HashMap<>();

    public static ConcurrentHashMap<String, RoadSegmentAnalysis> activeProcessSet = new ConcurrentHashMap<>();

    // Prototype constructor
    public RoadMonitoringApp(Settings s) {
        if (s.contains(ROADMONITOR_SETTING_SIMULTANEOUS_REQS)){
            SIMULTANEOUS_SEGMENT_REQS = s.getInt(ROADMONITOR_SETTING_SIMULTANEOUS_REQS);
        }

        if (s.contains(ROADMONITOR_SETTING_INTERVAL)){
            ANALYSIS_INTERVAL = s.getInt(ROADMONITOR_SETTING_INTERVAL);
        }

        if (s.contains(ROADMONITOR_SETTING_OVERRIDE_CLOUDFOG_RATIO)){
            ANALYSIS_ASSIGNMENT_CLOUDFOG_RATIO = s.getDouble(ROADMONITOR_SETTING_OVERRIDE_CLOUDFOG_RATIO);
        }

        // override repositories
        roadSegmentRepository = new TreeSet<>();
        fogServerRepository = new TreeSet<>();
        closestServerCache = new HashMap<>();


        super.setAppID(APP_ID);
    }


    // Copy-constructor
    public RoadMonitoringApp(RoadMonitoringApp a) {super(a);}

    public static void drawSegments(Graphics2D g2) {
        g2.setStroke(NodeGraphic.STROKE_DASHED);


        for (RoadSegmentAnalysis s : RoadMonitoringApp.activeProcessSet.values()) {
            g2.setColor(Color.magenta);
            if (s.fogMode) g2.setColor(Color.GREEN);


            g2.drawLine(
                    scale(s.getStartCoord().getX()), scale(s.getStartCoord().getY()),
                    scale(s.getEndCoord().getX()), scale(s.getEndCoord().getY()));

            g2.drawString(s.bus, scale(s.getStartCoord().getX()), scale(s.getStartCoord().getY()));
            g2.drawOval(scale(s.getEndCoord().getX())-5, scale(s.getEndCoord().getY())-5,10,10);

        }
        g2.setStroke(new BasicStroke());
        g2.setColor(Color.BLACK); // reset?
    }

    public static DTNHost getClosestServerFromCache(Coord coord) {
        DTNHost dtnHost = closestServerCache.get(coord);
        if (dtnHost == null){
            dtnHost = findClosestServer(coord);

        }
        return dtnHost;

    }

    /** returns null if no servers are known */
    private static DTNHost findClosestServer(Coord coord) {
        if (fogServerRepository.isEmpty()) return null;
        DTNHost dtnHost;
        dtnHost = fogServerRepository.stream().min(
                Comparator.comparingDouble((x -> x.getLocation().distance(coord)))).get();
        return dtnHost;
    }

    @Override
    public Message handle(Message msg, DTNHost host) {
        return null;
    }

    @Override
    public void update(DTNHost host) {
        if (SimClock.getTime() >= SimScenario.getInstance().getEndTime() - 3600){
            if (SimClock.getTime() == SimScenario.getInstance().getEndTime()){
                debugUncompletedProcesses();
            }
            return;
        }

        if (bootstrapFinished) {
            scheduleRoadAnalyses(host);

        } else {
            log.info("*** Sendding Cloud Init mesasge");
//            sendCloudProcessStartMessage(host);
            bootstrap(host);
        }
    }

    private void scheduleRoadAnalyses(DTNHost host) {
        List<RoadSegmentAnalysis> pendingRequests = acquireNewAnalysisRequests();
        if (pendingRequests.isEmpty()) return;

        //TODO remove this limit
        // Limit updates to analysis interval
        if ( SimClock.getIntTime() != 2 && SimClock.getIntTime() % ANALYSIS_INTERVAL != 0){ return;}
        //Choose noOfSegments unique road segments to analyse
        int noOfSegmentsToChoose = SIMULTANEOUS_SEGMENT_REQS;
        noOfSegmentsToChoose = Math.min(pendingRequests.size(), noOfSegmentsToChoose);
        HashSet<Integer> segmentIndexes = Util.uniqueRandomIntList(noOfSegmentsToChoose, 0, pendingRequests.size());

        for (Integer segmentIndex : segmentIndexes) {
            startSegmentAnalysis(pendingRequests.get(segmentIndex));

        }
    }

    private void debugUncompletedProcesses() {

        for (DTNHost host : SimScenario.getInstance().getHosts()) {



            SimulationApplicationEngine engine = ((BpmEngineApplication) host.getRouter().getApplications(BpmEngineApplication.APP_ID).iterator().next()).getEngine();

            if (!engine.getRunningInstances(false).isEmpty()){
                log.warn("-------Unfinished processes for host " + host + "----------");
            }
            for (ProcessInstance runningInstance : engine.getRunningInstances(true)) {
                ProcessEngine processEngine = engine.getProcessEngine();

                log.info("\n\tProcess: " + runningInstance.getProcessDefinitionName() + " , id: " + runningInstance.getProcessInstanceId());
                Map<String, Object> variables = runningInstance.getProcessVariables();
                if ( variables != null) variables.forEach((s, o) -> log.warn("\t" + s + " - " + o));

                List<Execution> executions = processEngine.getRuntimeService().createExecutionQuery().onlyChildExecutions().processInstanceId(runningInstance.getProcessInstanceId()).list();

                for (Execution execution : executions) {
                    log.info("\tExecution: " + execution.getName() + " - " + execution.getId() + ", " + execution.getActivityId());
                }
            }
        }

    }


    private void bootstrap(DTNHost host) {

        bootstrapFinished = true;
        populateRepositories();


        bpmEngineApp = BpmEngineApplication.ofHost(host);

        if (bpmEngineApp != null){
            bpmEngineApp.getAppListeners().add(this);
        }


    }


    private void populateRepositories() {
        populateBusRepository();
        populateFogRepository();
    }

    private void populateFogRepository() {
        List<DTNHost> hosts = SimScenario.getInstance().getHosts().stream()
                .filter(dtnHost -> dtnHost.getName().startsWith("fog"))
                .collect(Collectors.toList());

        fogServerRepository.addAll(hosts);

        // calculate for each road segment the closest fogserver.
        populateClosestServerCache();

    }

    private void populateClosestServerCache() {
        if (fogServerRepository.isEmpty()) return;
        for (RoadSegment segment : roadSegmentRepository) {
            closestServerCache.put(segment.getStartCoord(), findClosestServer(segment.getStartCoord()));
            closestServerCache.put(segment.getEndCoord(), findClosestServer(segment.getEndCoord()));
        }
    }


    private void populateBusRepository() {

        // TODO: eliminate duplicates (some segments may be shared between two lines)
        for (int line_i = 1; line_i <= 2; line_i++){

            List<Coord> stops = BusControlSystem.getBusControlSystem(line_i).getBusStops();//TODO hardcode

            for (int i = 1; i < stops.size(); i++) {
                roadSegmentRepository.add(new RoadSegment(stops.get(i-1), stops.get(i)));
            }
        }



        DoubleSummaryStatistics statistics = roadSegmentRepository.stream()
                .mapToDouble(x -> x.getStartCoord().distance(x.getEndCoord())).summaryStatistics();


        log.info("Finished populating RoadSegmentRepository. \n" +statistics);

    }

    public ArrayList<RoadSegmentAnalysis> acquireNewAnalysisRequests(){
        ArrayList<RoadSegmentAnalysis> requests = new ArrayList<>();
        for (RoadSegment segment: roadSegmentRepository){
            if (segment.isNewAnalysisDue()){
                requests.add(new RoadSegmentAnalysis(segment));
            }
        }
        return requests;
    }

    @Override
    public Application replicate() {
        log.info("Replicating RoadMonitoringApp App");
        return new RoadMonitoringApp(this);
    }

    public static Set<RoadSegment> getRoadSegmentRepository() {
        return roadSegmentRepository;
    }

    public static Set<DTNHost> getFogServerRepository() {
        return fogServerRepository;
    }

    public static DTNHost findClosestServerToCoord(){
        return null;
    }

    @Override
    public void gotEvent(String event, Object params, Application app, DTNHost host) {
        if (!app.appID.equals(BpmEngineApplication.APP_ID)) return;

        if (event.equals(PROCESS_COMPLETED)){
            ImmutablePair<Double, FlowableEngineEvent> p = (ImmutablePair<Double, FlowableEngineEvent>) params;
            if (p.getRight().getProcessDefinitionId().startsWith("roadanalysisprocessV2")) {
                finishSegmentAnalysis(p.getRight());
            }
        }

    }

    private void startSegmentAnalysis(RoadSegmentAnalysis request) {
        Map<String, Object> varsMap = new HashMap<String, Object>() {{
            put("segmentStartCoord", request.segment.getStartCoord().toString());
            put("segmentEndCoord",  request.segment.getEndCoord().toString());
            put("segmentAnalysisStartTime",  SimClock.getFormattedTime(1));
        }};

        if (ANALYSIS_ASSIGNMENT_CLOUDFOG_RATIO != RATIO_NO_OVERRIDE){

            varsMap.put("experimentCloudRatioOverride", ANALYSIS_ASSIGNMENT_CLOUDFOG_RATIO);
        }
        log.info("** Starting Segment Analysis Process #{} :\n Start: {}\n End: {}",
                analysisCounter, request.getStartCoord(), request.getEndCoord() );
        analysisCounter++;
        request.segment.isInProgress = true;

        final ProcessInstance startedProcess = bpmEngineApp.getEngine().startProcessInstance("roadanalysisprocessV2", varsMap);

        Object operatingMode = bpmEngineApp.getEngine().getProcessEngine().getRuntimeService().getVariable(startedProcess.getProcessInstanceId(), "operatingMode");
        if (operatingMode.equals("fog")) request.fogMode = true;//set as part of first task of process

        activeProcessSet.put(startedProcess.getProcessInstanceId(), request);
    }

    private void finishSegmentAnalysis(FlowableEngineEvent engineEvent) {
        final RoadSegmentAnalysis analysis = activeProcessSet.remove(engineEvent.getProcessInstanceId());
        analysis.segment.isInProgress = false;
        analysis.segment.lastAnalysed = SimClock.getTime();
    }
}
