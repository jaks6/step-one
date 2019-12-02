package ee.mass.epm.scenario.spe;

import org.flowable.engine.delegate.DelegateExecution;
import org.flowable.engine.delegate.JavaDelegate;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

public class CreateRoadAnalysisTask implements JavaDelegate {
    Logger log = LoggerFactory.getLogger(this.getClass());

    @Override
    public void execute(DelegateExecution execution) {


        log.info("CreateRoadAnalysisTask Execute");

        // Find a bus which is near the road start and moving towards road end
        execution.setVariable("resultsMessageExecution", execution.getProcessInstanceId());

        //execution.setVariable("videoFileRecipientAddress", execution.getVariable("localhost"));


    }
}
