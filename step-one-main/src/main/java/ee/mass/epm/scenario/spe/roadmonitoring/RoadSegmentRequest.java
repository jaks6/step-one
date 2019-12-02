package ee.mass.epm.scenario.spe.roadmonitoring;

import java.util.HashMap;
import java.util.Map;

public class RoadSegmentRequest {

    private RoadSegment segment;

    public static RoadSegmentRequest fromRoadSegment(RoadSegment segment) {
        RoadSegmentRequest request = new RoadSegmentRequest();
        request.segment = segment;
        return request;
    }

    public Map<String, Object> toProcessVariables() {
        HashMap<String, Object> variables = new HashMap<>();

        variables.put("segmentStartLocation", segment.getStartCoord());
        variables.put("segmentEndLocation", segment.getEndCoord());
        return variables;
    }
}


