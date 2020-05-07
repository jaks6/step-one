package ee.mass.epm.samples.bottomup;

import applications.bpm.BpmEngineApplication;
import core.DTNHost;
import core.SimScenario;
import ee.mass.epm.sim.CpuConf;
import org.flowable.engine.delegate.DelegateExecution;
import org.flowable.engine.delegate.JavaDelegate;

public class CreateLoadReportTask implements JavaDelegate {


    @Override
    public void execute(DelegateExecution execution) {

        DTNHost localhost = SimScenario.getInstance().getHosts()
                .get(execution.getVariable("localhost", Integer.class));

        // shows how much processing work is in queue (in MIPS)
        int queueSizeInMips = BpmEngineApplication.ofHost(localhost).getEngine().getQueuedSimTasksSize();

        CpuConf cpuConf = BpmEngineApplication.ofHost(localhost).getCpuConf();

        LoadReport loadReport = new LoadReport(localhost.getAddress());
        loadReport.noOfCpus = cpuConf.getNoOfCpus();
        loadReport.cpuSpeed = cpuConf.getCpuSpeed();
        loadReport.queueSizeMips = queueSizeInMips;

        execution.setVariable("loadReport", loadReport);
    }


}
