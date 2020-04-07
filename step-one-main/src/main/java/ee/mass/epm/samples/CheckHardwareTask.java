package ee.mass.epm.samples;

import applications.bpm.BpmEngineApplication;
import core.DTNHost;
import core.SimScenario;
import ee.mass.epm.SimulatedProcessEngineConfiguration;
import ee.mass.epm.sim.CpuConf;
import ee.mass.epm.sim.task.SimulatedTask;
import org.flowable.engine.delegate.DelegateExecution;
import org.flowable.engine.impl.context.Context;

public class CheckHardwareTask extends SimulatedTask {

    @Override
    public void execute(DelegateExecution execution) {
        super.execute(execution);

        SimulatedProcessEngineConfiguration engine =
                (SimulatedProcessEngineConfiguration) Context.getProcessEngineConfiguration();

        int hostAddress = engine.getHostAddress();
        DTNHost self = SimScenario.getInstance().getHosts().get(hostAddress);


        CpuConf cpuConf = BpmEngineApplication.ofHost(self).getCpuConf();
        execution.setVariable("noOfCpus", cpuConf.getNoOfCpus() );
        execution.setVariable("cpuSpeed", cpuConf.getCpuSpeed() );


    }
}
