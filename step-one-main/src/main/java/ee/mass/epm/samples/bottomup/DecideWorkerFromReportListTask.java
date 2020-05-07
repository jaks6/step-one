package ee.mass.epm.samples.bottomup;

import ee.mass.epm.sim.task.SimulatedTask;
import org.flowable.engine.delegate.DelegateExecution;

import java.util.Comparator;
import java.util.List;
import java.util.Optional;
import java.util.logging.Logger;

public class DecideWorkerFromReportListTask extends SimulatedTask {
    static Logger log = Logger.getLogger(DecideWorkerFromReportListTask.class.getName());
    /**
     * Make a decision based on available fog load reports
     */
    @Override
    public void execute(DelegateExecution execution) {
        super.execute(execution);

        if (!execution.hasVariable("reportList")){
            // TODO: some fallback behaviour
        }
        List<LoadReport> reportList = execution.getVariable("reportList", List.class);


        // Choose the one with lowest queue size. Note: We are not taking into account the CPU speed, no of CPUs..
        // Replace with a more complex decision-making
        // also makes sense to try to choose closer geographically fog nodes
        Optional<LoadReport> lowestLoadFog = reportList.stream()
                .min(Comparator.comparing(loadReport -> loadReport.queueSizeMips));

        if (lowestLoadFog.isPresent()){
            execution.setVariable("workerAddress", lowestLoadFog.get().hostAddress);
        }


    }
}
