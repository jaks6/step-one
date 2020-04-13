package ee.mass.epm.samples;

import applications.bpm.BpmEngineApplication;
import core.DTNHost;
import core.SimScenario;
import ee.mass.epm.sim.CpuConf;
import org.flowable.engine.delegate.DelegateExecution;
import org.flowable.engine.delegate.JavaDelegate;

public class CheckLoadTask implements JavaDelegate {


    @Override
    public void execute(DelegateExecution execution) {

        DTNHost localhost = SimScenario.getInstance().getHosts()
                .get(execution.getVariable("localhost", Integer.class));

        // shows how much processing work is in queue (in MIPS)
        int queueSizeInMips = BpmEngineApplication.ofHost(localhost).getEngine().getQueuedSimTasksSize();

        CpuConf cpuConf = BpmEngineApplication.ofHost(localhost).getCpuConf();
        execution.setVariable("noOfCpus", cpuConf.getNoOfCpus() );
        execution.setVariable("cpuSpeed", cpuConf.getCpuSpeed() );
        execution.setVariable("queueSizeMips", queueSizeInMips );

    }
}
