package ee.mass.epm.sim;

import org.flowable.engine.ProcessEngineConfiguration;
import org.flowable.engine.runtime.Execution;

import java.util.*;

public class SimulatedWorkQueue {


    private final ProcessEngineConfiguration processEngineConfiguration;
    private final int cpuSpeed;
    // Each CPU will hold a queue of jobs
    List<Queue<JobHandle>> cpus = new LinkedList<>();
    Deque<JobHandle> registeredTasks;



    public SimulatedWorkQueue(ProcessEngineConfiguration engineConf, int cpuSpeed, int totalCpus) {
        this.processEngineConfiguration = engineConf;
        this.registeredTasks = new LinkedList<>();
        this.cpuSpeed = cpuSpeed;

        for (int i = 0; i < totalCpus; i++) {
            cpus.add(new LinkedList<>());
        }

    }

    /** Estimate how many doWork() calls it will take to finish all currently present jobs */
    public int getTimeToFinishJobs(){

        int activeJobsSum = cpus.stream().mapToInt(
                jobHandles -> jobHandles.stream().mapToInt(JobHandle::getWorkLeft).sum()
        ).sum();
        return registeredTasks.stream().mapToInt(JobHandle::getSize).sum() + activeJobsSum;
    }

    public int doWork(){
        int workDone = 0;


        for (Queue<JobHandle> cpuActiveJobs : cpus){
            int cpuInstructionsLeft = cpuSpeed;
            while (cpuInstructionsLeft > 0 && (!cpuActiveJobs.isEmpty()||!registeredTasks.isEmpty())) {

                // take some new work
                if (cpuActiveJobs.isEmpty()){
                    JobHandle newJobHandle = registeredTasks.pollFirst();
                    if (newJobHandle != null){
                        cpuActiveJobs.add(newJobHandle);
                    }
                }

                JobHandle job = cpuActiveJobs.element();
                cpuInstructionsLeft = job.work(cpuInstructionsLeft);

                if (job.isFinished()) {
                    cpuActiveJobs.remove();
                    // send trigger so that execution can continue
                    processEngineConfiguration.getRuntimeService().trigger(job.getExecutionId());
                }

            }
            workDone += cpuSpeed - cpuInstructionsLeft;
        }
        return workDone;
    }

    public JobHandle addJob(String id, int size) {
        JobHandle jobHandle = new JobHandle(id, size);
        registeredTasks.add(jobHandle);
        return jobHandle;
    }

    public void removeJobs(String processInstanceId) {
        List<Execution> list = processEngineConfiguration.getRuntimeService().createExecutionQuery()
                .processInstanceId(processInstanceId).onlyChildExecutions().list();


        for (Execution execution : list) {
            removeExecutionFromJobs(execution.getId());
        }

    }

    private void removeExecutionFromJobs(String executionId) {
        JobHandle job;
        //Go through registeredTask list
        for(Iterator<JobHandle> i = registeredTasks.iterator(); i.hasNext();) {
            job = i.next();

            if (job.getExecutionId().equals(executionId)){
//                    System.out.println("Removed job of processInstanceId = [" + processInstanceId + "]");
                i.remove();
            }
        }
        //Go through activeJobs list for each cpu
        for (Queue<JobHandle> cpuActiveJobs : cpus) {
            for( Iterator<JobHandle> j = cpuActiveJobs.iterator(); j.hasNext();) {
                job = j.next();
                if (job.getExecutionId().equals(executionId)){
    //                    System.out.println("Removed job of processInstanceId = [" + processInstanceId + "]");
                    j.remove();
                }
            }
        }
    }

    public void cancelJob(String executionId) {
            removeExecutionFromJobs(executionId);
    }
}
