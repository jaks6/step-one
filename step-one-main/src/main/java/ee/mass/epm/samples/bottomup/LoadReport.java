package ee.mass.epm.samples.bottomup;

import core.SimClock;

import java.io.Serializable;

public class LoadReport implements Serializable {

    private static final long serialVersionUID = 123L;

    public int hostAddress;
    public int timestamp;
    public int noOfCpus;
    public int cpuSpeed;
    public int queueSizeMips;

    public LoadReport(int address) {
        this.hostAddress = address;
        this.timestamp = SimClock.getIntTime();
    }

    @Override
    public String toString() {
        return "[" + hostAddress + "]queueSize: "+ queueSizeMips+" cpus:" + noOfCpus;
    }
}