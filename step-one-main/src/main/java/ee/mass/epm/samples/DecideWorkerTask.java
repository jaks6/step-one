package ee.mass.epm.samples;

import core.DTNHost;
import core.SimScenario;
import ee.mass.epm.sim.task.SimulatedTask;
import org.flowable.engine.delegate.DelegateExecution;

import java.util.List;

public class DecideWorkerTask  extends SimulatedTask {
    private static final double QUEUE_TIME_THRESHOLD = 100; // seconds. consider using a process variable

    /**
     * Decide whether to use Fog or Cloud based on information about Fog hosts' CPU configuration,
     * the size of CPU job queue of the Fog, and the size of the job item we want to add to the Fog.
     * If the the estimated waiting time in queue is below a theshold, use the fog, otherwise use cloud.
     * @param execution
     */
    @Override
    public void execute(DelegateExecution execution) {
        super.execute(execution);


        // Information we received from the fog node previously:
        int reportedLoad = execution.getVariable("queueSizeMips", Integer.class);
        int cpuSpeed = execution.getVariable("cpuSpeed", Integer.class);
        int totalCpus = execution.getVariable("noOfCpus", Integer.class);

        // information about the job we want to add:
        int workSize = execution.getVariable("workSize", Integer.class);


        // We can estimate no. of jobs on server if we assume all jobs on server have the same size (workSize) for this example
        double estimateNoOfWorksInQueue = Math.ceil(reportedLoad / (double) workSize);

        int activeCPUs = Math.min ( (int) estimateNoOfWorksInQueue, totalCpus);

        // how long to finish my new job:
        double estimatedTimeToMyWork = workSize / (double) cpuSpeed;

        double estimatedTotalWaitingTime;
        if (activeCPUs < totalCpus) {
            estimatedTotalWaitingTime = estimatedTimeToMyWork;
        } else {
            double estimatedTimeToFinishQueue = reportedLoad / (double) ( cpuSpeed * activeCPUs);
            estimatedTotalWaitingTime = estimatedTimeToFinishQueue + estimatedTimeToMyWork;
        }

        List<DTNHost> hostList = SimScenario.getInstance().getHosts();

        int fogServerAddress = execution.getVariable("fogServerAddress", Integer.class);

        DTNHost cloud = hostList.stream().filter(dtnHost -> dtnHost.getName().startsWith("Cloud")).findFirst().orElse(null);

        if ( estimatedTotalWaitingTime <= QUEUE_TIME_THRESHOLD ) {
            execution.setVariable("workerAddress", fogServerAddress);
        } else {
            execution.setVariable("workerAddress", cloud.getAddress());
        }


    }
}
