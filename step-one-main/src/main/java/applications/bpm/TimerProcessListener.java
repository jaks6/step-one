package applications.bpm;

import core.Application;
import core.ApplicationListener;
import core.DTNHost;
import org.apache.commons.lang3.tuple.ImmutablePair;
import org.flowable.common.engine.api.delegate.event.FlowableEngineEvent;

import static report.BpmAppReporter.PROCESS_STARTED;

public class TimerProcessListener implements ApplicationListener {

    public void gotEvent(String event, Object params, Application app,
                         DTNHost host) {
        // Check that the event is sent by correct application type
        if (!(app instanceof BpmEngineApplication)) return;

        if (event.equals(PROCESS_STARTED)){
            ImmutablePair<Double, FlowableEngineEvent> pair = (ImmutablePair<Double, FlowableEngineEvent>)params;


            FlowableEngineEvent right = pair.getRight();
        }






    }
}
