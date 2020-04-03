package ee.mass.epm.sim;

public class CpuConf {

    private static final int DEFAULT_CPU_SPEED = 10000;
    private static final int DEFAULT_NO_OF_CPUS = 1;
    int cpuSpeed;
    int noOfCpus;

    public int getCpuSpeed() {
        return cpuSpeed;
    }

    public void setCpuSpeed(int cpuSpeed) {
        this.cpuSpeed = cpuSpeed;
    }

    public int getNoOfCpus() {
        return noOfCpus;
    }

    public void setNoOfCpus(int noOfCpus) {
        this.noOfCpus = noOfCpus;
    }

    public CpuConf(int cpuSpeed, int noOfCpus) {
        this.cpuSpeed = cpuSpeed;
        this.noOfCpus = noOfCpus;
    }

    public CpuConf () {
        this.cpuSpeed = DEFAULT_CPU_SPEED;
        this.noOfCpus = DEFAULT_NO_OF_CPUS;
    }
}
