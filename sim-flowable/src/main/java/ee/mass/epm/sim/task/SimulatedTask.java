package ee.mass.epm.sim.task;

import ee.mass.epm.sim.JobHandle;
import ee.mass.epm.SimulatedProcessEngineConfiguration;
import org.flowable.bpmn.model.ServiceTask;
import org.flowable.common.engine.api.delegate.Expression;
import org.flowable.engine.delegate.DelegateExecution;
import org.flowable.engine.impl.context.Context;
import org.flowable.engine.impl.delegate.TriggerableActivityBehavior;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

/** A Task which represents generic processing work done. The amount of work is specified by a jobsize parameter,
 *  on the basic level, the jobsize reflects how many ONE simulator ticks it takes for the job to finish.
 *  Jobsize can be set either through process variables or as expression fields.
  */
public class SimulatedTask  extends ServiceTask implements TriggerableActivityBehavior {

    Expression work_size;
    Logger log = LoggerFactory.getLogger(this.getClass());


    @Override
    public void execute(DelegateExecution execution) {

        SimulatedProcessEngineConfiguration engine = (SimulatedProcessEngineConfiguration) Context.getProcessEngineConfiguration();

        int jobsize = 0;
        if (work_size != null){
            jobsize = Integer.parseInt( (String) work_size.getValue(execution) );

        }

        JobHandle handle = engine.getSimulatedWorkQueue().addJob(execution.getId(), jobsize);

//        System.out.println("Simulated Task created, Job Handle: [" + handle + "]");
        log.debug("started execution = [" + execution + "], jobSize =" + jobsize);
    }


    @Override
    public void trigger(DelegateExecution execution, String signalEvent, Object signalData) {
        log.debug("finished execution = [" + execution + "]");
    }
}
