package applications.bpm;

import core.DTNHost;
import core.Message;
import core.NetworkInterface;
import ee.mass.epm.sim.CpuConf;
import org.apache.commons.lang3.NotImplementedException;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

public class CostHelper {


    private enum CostGranularity { SECOND, HOUR }
    private double costPerUnit;
    private CostGranularity granularity;
    private DTNHost host;
    private double[] interfaceCosts;

    static Logger log = LoggerFactory.getLogger(CostHelper.class.getName());



    void setCpuCost(String granularity, double value) {
        switch (granularity.toUpperCase()){
            case "SECOND":
            case "SECONDS":
                this.granularity = CostGranularity.SECOND;
                break;

            case "HOUR":
            case "HOURS":
                this.granularity = CostGranularity.HOUR;
                break;
            default:
                log.error("Unknown granularity specified for cpu cost. Valid values are second or hour, defaulting to hour");
                this.granularity = CostGranularity.HOUR;
                break;
        }
        this.costPerUnit = value;

    }

    void setCpuCost(double[] interfaceCosts) {
        this.interfaceCosts = interfaceCosts;
    }

    void init(DTNHost host) {
        this.host = host;

        // validate that no. of interfaces matches the cost parameters
        if ( interfaceCosts != null && host.getInterfaces().size() > interfaceCosts.length ) {

            log.error("No. of interfaces on host {} is greater than number of network interface costs provided!", this.host);
            System.exit(1);
        }
    }

    public static CostHelper ofHost(DTNHost host){
        throw new NotImplementedException("Not implemented");
    }

    public static double getCostForTransmission(Message msg, NetworkInterface ni) {
        return getCostForTransmission(msg.getSize(),ni);

    }

    /**
     * Calculate the monetary cost of transmitting messageSize bytes on the provided network interface
     * @param messageSize
     * @param ni
     * @return the monetary cost. The granularity is up to the designer, as specified in simulation configuration
     */
    public static double getCostForTransmission(int messageSize, NetworkInterface ni) {
        int interfaceIndex = ni.getHost().getInterfaces().indexOf(ni);

        BpmEngineApplication bpmApp = BpmEngineApplication.ofHost(ni.getHost());

        double costPerMegabyte = bpmApp.getCostHelper().interfaceCosts[interfaceIndex];

        return messageSize / 1000000f * costPerMegabyte;
    }

    /**
     * Calculate the monetary cost of processing 'worksize' amount of work on the given hosts CPU.
     * When calculating the cost, the amount of time the hosts CPU takes to process the task is rounded up to the nearest
     * cost granularity as defined for the host cpuCostGranularity simulation parameter.
     * @param worksize MI (millions operations) - size of the job
     * @param host the DTNHost whose CPU cost parameters the price is based on
     * @return the monetary cost, based on how long it would take the given host to finish the job and
     * price parameters of the simulation
     */
    public static double getCostForTask(int worksize, DTNHost host) {

        BpmEngineApplication bpmApp = BpmEngineApplication.ofHost(host);

        CpuConf cpuConf = bpmApp.getCpuConf();
        CostHelper costHelper = bpmApp.getCostHelper();

        double timeInSeconds = (double) worksize / cpuConf.getCpuSpeed();

        double workSizeModifier = costHelper.granularity == CostGranularity.HOUR ? 3600.0 : 1.0;
        return Math.ceil(timeInSeconds / workSizeModifier) * costHelper.costPerUnit;
    }
}
