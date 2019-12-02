package ee.mass.epm.sim;

public class CpuConf {

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
}
