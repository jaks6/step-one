package ee.mass.epm.samples;

import core.Connection;
import core.DTNHost;
import core.SimScenario;
import ee.mass.epm.sim.task.SimulatedTask;
import org.flowable.engine.delegate.DelegateExecution;

import java.util.Comparator;
import java.util.List;
import java.util.Optional;

public class CheckConnectedFogServers extends SimulatedTask {
    @Override
    public void execute(DelegateExecution execution) {
        super.execute(execution);

        List<DTNHost> hostList = SimScenario.getInstance().getHosts();

        // localhost is the one executing this task:
        DTNHost localhost = hostList.get(execution.getVariable("localhost", Integer.class));


        // Find all connected nodes with name starting with "Fog" and choose the onewith fastest connection
        Optional<Connection> fastestConnection = localhost.getConnections().stream()
                .filter(c -> c.isUp() && c.getOtherNode(localhost).getName().startsWith("Fog"))
                .max(Comparator.comparing(Connection::getSpeed));


        if (fastestConnection.isPresent()){
            int fogAddress = fastestConnection.get().getOtherNode(localhost).getAddress();
            execution.setVariable("fogServerAddress", fogAddress);
        }


    }
}
