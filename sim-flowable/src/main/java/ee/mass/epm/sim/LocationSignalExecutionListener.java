package ee.mass.epm.sim;

import ee.mass.epm.SimulatedProcessEngineConfiguration;
import ee.mass.epm.Util;
import org.flowable.common.engine.api.delegate.Expression;
import org.flowable.engine.delegate.DelegateExecution;
import org.flowable.engine.delegate.ExecutionListener;
import org.flowable.engine.impl.context.Context;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

public class LocationSignalExecutionListener implements ExecutionListener {

    /**
     *
     * Valid formats for coordinate are (whitespace is ignored):
     * 1) (123.0, 232.0) - this format is toString() of core.Coord of ONE sim
     * 2) 123.0, 232.0
     * 3) (123.0; 232.0)
     * 4) 123.0; 232.0
     */
    Expression coordinate;
    Logger log = LoggerFactory.getLogger(this.getClass());


    @Override
    public void notify(DelegateExecution execution) {
        SimulatedProcessEngineConfiguration engine = (SimulatedProcessEngineConfiguration) Context.getProcessEngineConfiguration();

        String coordString = (String) coordinate.getValue(execution);



        engine.getLocationSignalSubscriptions().add(
                new LocationSignalSubscription(execution.getId(), Util.extractCoordFromString(coordString)));
    }
}
