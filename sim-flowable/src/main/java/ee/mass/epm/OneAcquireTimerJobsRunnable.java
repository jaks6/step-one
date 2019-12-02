package ee.mass.epm;

import org.flowable.job.service.impl.asyncexecutor.AcquireTimerJobsRunnable;
import org.flowable.job.service.impl.asyncexecutor.AsyncExecutor;
import org.flowable.job.service.impl.asyncexecutor.JobManager;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

public class OneAcquireTimerJobsRunnable extends AcquireTimerJobsRunnable {
    private static final Logger LOGGER = LoggerFactory.getLogger(OneAcquireTimerJobsRunnable.class);


    public OneAcquireTimerJobsRunnable(AsyncExecutor asyncExecutor, JobManager jobManager) {
        super(asyncExecutor, jobManager);
    }

    @Override
    public synchronized void run() {
       LOGGER.info("Running disabled OneAcquireTimerJobs");
    }
}
