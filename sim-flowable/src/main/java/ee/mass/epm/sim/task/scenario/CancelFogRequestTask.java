package ee.mass.epm.sim.task.scenario;

import ee.mass.epm.SimulatedProcessEngineConfiguration;
import org.flowable.engine.delegate.DelegateExecution;
import org.flowable.engine.delegate.JavaDelegate;
import org.flowable.engine.impl.context.Context;

import java.util.HashSet;

public class CancelFogRequestTask implements JavaDelegate {
    @Override
    public void execute(DelegateExecution execution) {
        //TODO: This is currently a very hacky implementation, assumes there is at least one pending request and only deletes that first one.

        SimulatedProcessEngineConfiguration engine = (SimulatedProcessEngineConfiguration) Context.getProcessEngineConfiguration();

        HashSet<Integer> pendingRequests = (HashSet<Integer>) execution.getVariable(FogRequestTask.PROCESS_VAR_PENDING_FOGREQUESTS);
        Integer pending = pendingRequests.iterator().next();

        engine.getFogMessageRequests().removeIf(req -> req.hashCode() == pending);


    }
}
