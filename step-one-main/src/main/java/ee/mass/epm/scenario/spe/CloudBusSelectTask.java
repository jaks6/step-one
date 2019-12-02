package ee.mass.epm.scenario.spe;

import applications.bpm.BpmEngineApplication;
import applications.bpm.Util;
import applications.spe.RoadMonitoringApp;
import core.Coord;
import core.DTNHost;
import core.SimScenario;
import movement.BusMovement;
import movement.map.MapNode;
import org.apache.commons.lang3.tuple.Pair;
import org.flowable.engine.delegate.DelegateExecution;
import org.flowable.engine.delegate.JavaDelegate;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

import java.util.Comparator;
import java.util.List;
import java.util.stream.Collectors;

public class CloudBusSelectTask  implements JavaDelegate {
    public static final double STOP_DISTANCE_THRESHOLD = 5.0;
    private static final int MAX_RUNNING_PROCESSES_ALLOWED = 50;
    Logger log = LoggerFactory.getLogger(this.getClass());

    @Override
    public void execute(DelegateExecution execution) {
        // Find a bus which is near the road start and moving towards road end
        String startCoordString = (String) execution.getVariable("segmentStartCoord");
        String endCoordString = (String) execution.getVariable("segmentEndCoord");

        Coord startCoord = Util.extractCoordFromString(startCoordString);
        Coord endCoord = Util.extractCoordFromString(endCoordString);

        DTNHost closestBus = SimScenario.getInstance().getHosts().stream()
                .filter(x->x.getName().startsWith("B"))
                .map(x-> Pair.of(x, findBusDistanceToCoord(x, startCoord, endCoord)))
                .filter( x-> x.getRight() != -1 && x.getLeft().getComBus()
                        .getDouble(BpmEngineApplication.NO_OF_PROCESSES_RUNNING, 0) <= MAX_RUNNING_PROCESSES_ALLOWED)
                .min(Comparator.comparing(Pair::getRight))
                .map(Pair::getLeft)
//                .min(Comparator.comparing(dtnHost -> dtnHost.getLocation().distance(startCoord)))
                .orElse(null);

        if (closestBus != null){
            execution.setVariable("chosenBusAddress", closestBus.getAddress());
            execution.setVariable("videoFileRecipientAddress", execution.getVariable("localhost"));

            RoadMonitoringApp.activeProcessSet.get(execution.getVariable("resultsProcessInstanceId"))
                    .bus = closestBus.getName();
            log.info(String.format("** Chose Bus for Segment Analysis Process:Bus: %s, %s @ %s\n Start: %s\n End: %s",
                    closestBus.getName(),closestBus.getAddress(), closestBus.getLocation(),startCoord, endCoord ));
        } else {
            RoadMonitoringApp.activeProcessSet.remove(execution.getVariable("resultsProcessInstanceId"));

            log.error("Could not find a bus matching segment analysis request! start " + startCoord+ ", end "+ endCoord);

        }
    }


    /** Number of stops from bus to the given coordinate, or -1 if not on path */
    private int findBusDistanceToCoord(DTNHost x, Coord start, Coord end) {
        BusMovement movement = (BusMovement) x.getMovement();
        final List<MapNode> stops = movement.getStops();
        final List<Coord> stopsList = stops.stream().map(MapNode::getLocation).collect(Collectors.toList());
        final Pair<Integer, Integer> segmentIndexes = findSegmentIndexes(stopsList, start, end);

        //TODO: account for segment end index as well
        if (segmentIndexes.getLeft() != -1 && segmentIndexes.getRight() != -1){
            int segmentStart_i = segmentIndexes.getLeft();
            int segmentEnd_i = segmentIndexes.getRight();

            int startIndexDistance = segmentStart_i - movement.getRoute().getStopIndex();
            if (startIndexDistance < 0){
                startIndexDistance = stops.size() + startIndexDistance;
                // search the other way along the path
//                startIndexDistance = Math.floorMod(startIndexDistance, stops.size());
            }
            log.debug("-- " + x.getName() + " segmentStart_I: " + segmentStart_i+ ", busIndex: " + movement.getRoute().getStopIndex()+ ", distance: " + startIndexDistance);

            return startIndexDistance;
        }


        return -1;
    }

    /** Find the indexes of given start and stop coordinates inside the given list of coordinates
     * Returns -1 if coordinate not found
     * @param stops
     * @param start
     * @param end
     * @return
     */
    private Pair<Integer, Integer> findSegmentIndexes(List<Coord> stops, Coord start, Coord end) {
        int start_i = -1;
        int end_i = -1;
        for (int i = 0; i < stops.size(); i++) {
            if (start_i != -1 && end_i != -1) break;
            if ( start_i == -1 && stops.get(i).distance(start) <= STOP_DISTANCE_THRESHOLD) {
                // try the next neighbouring stops
                Coord nextStop = (stops.size() == i+1) ? stops.get(0) : stops.get(i+1);

                if ( nextStop.distance(end) <= STOP_DISTANCE_THRESHOLD){
                    start_i = i;
                    end_i = i+1;
                }
            }
//            if ( end_i == -1 && stops.get(i).distance(end) <= STOP_DISTANCE_THRESHOLD ) {
//                end_i = i;
//            }
        }
        return Pair.of(start_i, end_i);
    }
}
