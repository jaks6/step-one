package ee.mass.epm.samples.seminar;

import core.SimClock;
import ee.mass.epm.sim.task.SimulatedTask;
import org.flowable.engine.delegate.DelegateExecution;

public class FakeSensor extends SimulatedTask {
    @Override
    public void execute(DelegateExecution execution) {
        int time = SimClock.getIntTime();
        execution.setVariable("sensorTime", time);


    }
}
