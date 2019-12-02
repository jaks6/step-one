package ee.mass.epm.scenario.spe.roadmonitoring;

import core.Coord;
import core.SimClock;
import applications.spe.RoadMonitoringApp;

public class RoadSegment implements Comparable<RoadSegment>{


    public boolean isInProgress;
    Coord startCoord;
    Coord endCoord;

    public double lastAnalysed = -1;


    public RoadSegment (Coord start, Coord end){
        startCoord = start;
        endCoord = end;
    }
    public RoadSegment(double startX, double startY, double endX, double endY) {
        startCoord = new Coord(startX,startY);
        endCoord = new Coord(endX,endY);
    }

    public boolean isNewAnalysisDue(){
        return !isInProgress && (lastAnalysed == -1 || (SimClock.getTime() >= lastAnalysed + RoadMonitoringApp.ANALYSIS_INTERVAL));
    }


    public Coord getStartCoord() {
        return startCoord;
    }

    public Coord getEndCoord() {
        return endCoord;
    }

    /**
     * Compares RoadSegment only based on hashcode, which is arbitrary
     * @param other
     * @return result of Integer.compare( .. ) of the hashcodes
     */
    @Override
    public int compareTo(RoadSegment other) {

        return (getVector(this).compareTo(getVector(other)));
//        return Integer.compare(other.hashCode(), this.hashCode());

    }

    public static Coord getVector(RoadSegment segment){
        return new Coord(
                segment.endCoord.getX() - segment.startCoord.getX(),
                segment.endCoord.getY() - segment.startCoord.getY()
        );
    }
}
