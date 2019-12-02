package ee.mass.epm.sim;

import core.Coord;

public class LocationSignalSubscription {

    public String srcExecutionId;

    Coord coordinate;

    public LocationSignalSubscription(String srcExecutionId, Coord coordinate) {
        this.srcExecutionId = srcExecutionId;
        this.coordinate = coordinate;
    }

    public Coord getCoordinate() {
        return coordinate;
    }

    public String getExecutionId() { return srcExecutionId; }
}
