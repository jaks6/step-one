package ee.mass.epm.sim.task.scenario;


import ee.mass.epm.FogMessageRequest;
import ee.mass.epm.SimulatedProcessEngineConfiguration;
import org.flowable.engine.delegate.DelegateExecution;
import org.flowable.engine.delegate.JavaDelegate;
import org.flowable.engine.impl.context.Context;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

import java.util.HashSet;

/** Notifies the adaptive module of a request for a new adaptive fog-based message sending*/
public class FogRequestTask implements JavaDelegate {


    static final String PROCESS_VAR_PENDING_FOGREQUESTS = "pending_fogRequests";

    Logger log = LoggerFactory.getLogger(this.getClass());


    @Override
    public void execute(DelegateExecution execution) {
        log.debug("started execution = [" + execution + "]");


        SimulatedProcessEngineConfiguration engine = (SimulatedProcessEngineConfiguration) Context.getProcessEngineConfiguration();

        //TODO: read params from process fog or from field injection
        FogMessageRequest request = new FogMessageRequest(execution, 1, 999);

        engine.getFogMessageRequests().add(request);

        //So that we can later erase it if we wish
        putInProcessVariables(execution, request);

    }

    private void putInProcessVariables(DelegateExecution execution, FogMessageRequest request) {

        //TODO: consider using activity IDs or something to enable better managing/erasing of simulatenous requests
//        String currentActivityId = execution.getCurrentActivityId();
        HashSet<Integer> variable;
        if (execution.hasVariable(PROCESS_VAR_PENDING_FOGREQUESTS)){
            variable = (HashSet<Integer>) execution.getVariable(PROCESS_VAR_PENDING_FOGREQUESTS);
        } else {
            variable = new HashSet<>();
        }
        variable.add(request.hashCode());
        execution.setVariable(PROCESS_VAR_PENDING_FOGREQUESTS, variable);
    }
}
