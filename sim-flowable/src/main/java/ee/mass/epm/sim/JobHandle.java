package ee.mass.epm.sim;

public class JobHandle {
    private final int size;
    private final String executionId;
    private int workLeft;
    private int workDone;

    public JobHandle(String id, int size) {
        this.executionId = id;
        this.workDone = 0;
        this.size = size;
        this.workLeft = size;
    }

    public boolean isFinished() {
        return workLeft <= 0;
    }

    public void work() {
        workDone++;
    }

    public int getWorkDone() {
        return workDone;
    }

    public int getWorkLeft() { return workLeft; }

    public int getSize() {
        return size;
    }

    public String getExecutionId() {
        return executionId;
    }

    @Override
    public String toString() {
        return String.format("JobHandle %s [%s/%s]", executionId, size-workLeft, size);
    }

    /** Update work done on this job.
     *
     * @param cpuInstructionsLeft
     * @return how much many instructions were left unused after working on this job.
     */
    public int work(int cpuInstructionsLeft) {
        workLeft -= cpuInstructionsLeft;

        if (workLeft < 0){
            return Math.abs(workLeft);
        } else {
            return 0;
        }

    }
}
