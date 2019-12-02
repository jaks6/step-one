//package applications.bpm;
//
//import applications.fog.AdaptiveMobileApplication;
//import applications.fog.FogCandidate;
//import core.Application;
//import core.DTNHost;
//import core.ModuleCommunicationListener;
//import core.Settings;
//import ee.mass.epm.Engine;
//import ee.mass.epm.FogMessageRequest;
//import org.slf4j.Logger;
//import org.slf4j.LoggerFactory;
//
//import java.util.ArrayList;
//import java.util.HashMap;
//
//import static applications.fog.AdaptiveMobileApplication.COMBUS_PENDING_FOG_REQS;
//import static applications.fog.AdaptiveMobileApplication.PROP_SELECTED_SERVER;
//
//public class FogBpmEngineApplication extends BpmEngineApplication implements ModuleCommunicationListener {
//    public static final String APP_ID = "ee.ut.cs.mc.FogBpmApplication";
//    Logger log = LoggerFactory.getLogger(this.getClass());
//
//
//    private static final String SIGNAL_FOG_CANDIDATE = "Fog Server Signal";
//
//    private boolean isMobile = false;
//
//    public FogBpmEngineApplication(Settings s) {
//        super(s);
//        super.setAppID(APP_ID);
//        log.info("FogBpmApp Proto");
//    }
//
//    public FogBpmEngineApplication(FogBpmEngineApplication e) {
//        super(e);
//    }
//
//
//    @Override
//    public Application replicate() {
//        return new FogBpmEngineApplication(this);
//    }
//
//
//
//    @Override
//    public void update(DTNHost host) {
//        if (firstUpdate){ bootstrap(host); }
//        super.update(host);
//        if (isMobile) handlePendingFogRequests(host);
//    }
//
//    @Override
//    void bootstrap(DTNHost host) {
//        super.bootstrap(host);
//        isMobile = !host.getRouter().getApplications(AdaptiveMobileApplication.APP_ID).isEmpty();
//        host.getComBus().subscribe(PROP_SELECTED_SERVER, this);
//
//
//    }
//
//    private void handlePendingFogRequests(DTNHost host) {
//
//        ArrayList<FogMessageRequest> combusPendingRequests = Util.getArrayListFromComBus(mHost, COMBUS_PENDING_FOG_REQS);
//
////        if (host.getComBus().containsProperty(COMBUS_PENDING_FOG_REQS)){
////            combusPendingRequests = (ArrayList<FogMessageRequest>) host.getComBus().getProperty(COMBUS_PENDING_FOG_REQS);
////        } else {
////            combusPendingRequests = new ArrayList<>();
////        }
//        combusPendingRequests.addAll(engine.getFogMessageRequests());
//        engine.getFogMessageRequests().clear();
//
//        host.getComBus().updateProperty(COMBUS_PENDING_FOG_REQS,  combusPendingRequests );
//    }
//
//    @Override
//    public void moduleValueChanged(String key, Object newValue) {
//        if (key.equals(PROP_SELECTED_SERVER)){
//            if (newValue!= null)
//                handleFogModelResults((FogCandidate) newValue);
//        }
//    }
//
//    private void handleFogModelResults(FogCandidate candidate) {
//        //TODO: specify execution ID!
////        String executionId = candidate.getOriginalRequest().getProcessInstanceId();
//
//        HashMap<String, Object> processVars = new HashMap<>();
//        processVars.put("fogCandidateAddress", candidate.getHost().getAddress());
//        engine.signal(SIGNAL_FOG_CANDIDATE, processVars);
//    }
//
//    public boolean isMobile() {
//        return isMobile;
//    }
//}
