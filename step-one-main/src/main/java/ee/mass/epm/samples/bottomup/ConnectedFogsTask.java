package ee.mass.epm.samples.bottomup;

import core.Connection;
import core.DTNHost;
import core.SimScenario;
import org.flowable.engine.delegate.DelegateExecution;
import org.flowable.engine.delegate.JavaDelegate;

import java.util.List;
import java.util.stream.Collectors;

public class ConnectedFogsTask implements JavaDelegate {
    @Override
    public void execute(DelegateExecution execution) {

        List<DTNHost> hostList = SimScenario.getInstance().getHosts();

        // localhost is the one executing this task:
        DTNHost localhost = hostList.get(execution.getVariable("localhost", Integer.class));

        int inquirySource;
        // node that this fog report inquiry
        if (execution.hasVariable("startMessageSenderAddress")){
            inquirySource = execution.getVariable("startMessageSenderAddress", Integer.class);
        } else {
            inquirySource = -1;
        }

        // Find all connected nodes with name starting with "Fog" and create a list of their addresses
        List<Integer> connectedFogList = localhost.getConnections().stream()
                .filter(Connection::isUp)
                .map(c -> c.getOtherNode(localhost))
                .filter(host -> shouldPropagateToHost(host, inquirySource))
                .map(DTNHost::getAddress)
                .collect(Collectors.toList());

        Integer noOfHops = Integer.valueOf(String.valueOf(execution.getVariable("noOfHops")));

        execution.setVariable("noOfHops", --noOfHops);
        execution.setVariable("workSize", 10000000);
        execution.setVariable("connectedFogList", connectedFogList);


    }

    private boolean shouldPropagateToHost(DTNHost host, int inquirySource) {
        return ((host.getName().startsWith("Fog") || host.getName().startsWith("Cloud")) &&
                host.getAddress() != inquirySource) ;
    }
}
