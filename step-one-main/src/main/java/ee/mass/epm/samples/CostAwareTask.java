package ee.mass.epm.samples;

import applications.bpm.CostHelper;
import core.DTNHost;
import core.SimScenario;
import org.flowable.common.engine.api.delegate.Expression;
import org.flowable.engine.delegate.DelegateExecution;
import org.flowable.engine.delegate.JavaDelegate;

public class CostAwareTask implements JavaDelegate {
    Expression taskWorkSize;
    Expression taskBandwidthExpr;
    @Override
    public void execute(DelegateExecution execution) {

        DTNHost cloud;
        int taskWorkSize;
        int taskBandwidth;

        // Assign values to above, e.g. from process variables,
        // process field expressions or from simulation world

        taskWorkSize = execution.getVariable("someTaskWorkSize", Integer.class);

        taskBandwidth = (Integer) taskBandwidthExpr.getValue(execution);




        cloud = SimScenario.getInstance().getHosts().stream()
                .filter(h -> h.getName().startsWith("cloud"))
                .findFirst().orElse(null);


        double processingCost = CostHelper.getCostForTask(taskWorkSize, cloud);
        double transmissionCost = CostHelper.getCostForTransmission(
                taskBandwidth, cloud.getInterface(1));


        // Make decision based on cost, set process variables accordingly, etc.


    }
}
