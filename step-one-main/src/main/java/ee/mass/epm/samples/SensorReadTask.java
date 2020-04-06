package ee.mass.epm.samples;

import ee.mass.epm.sim.task.SimulatedTask;
import org.flowable.engine.delegate.DelegateExecution;

public class SensorReadTask extends SimulatedTask {

    @Override
    public void execute(DelegateExecution execution) {
        super.execute(execution);

        execution.setVariable("sensorValue", Math.random() );


    }
}
