package ee.mass.epm.scenario.spe;

import ee.mass.epm.scenario.spe.roadmonitoring.RoadSegment;
import core.Coord;

public class RoadSegmentAnalysis {

    public RoadSegment segment;
    public String bus = "";
    public boolean fogMode = false;

    public RoadSegmentAnalysis(RoadSegment segment) {
        this.segment = segment;
    }

    public Coord getStartCoord(){
        return segment.getStartCoord();
    }
    public Coord getEndCoord(){
        return segment.getEndCoord();
    }

    public void failProcess() {
    }
}
