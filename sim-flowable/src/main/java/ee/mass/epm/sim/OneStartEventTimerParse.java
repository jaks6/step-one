package ee.mass.epm.sim;

import ee.mass.epm.Engine;
import org.flowable.bpmn.model.BaseElement;
import org.flowable.bpmn.model.EventDefinition;
import org.flowable.bpmn.model.StartEvent;
import org.flowable.bpmn.model.TimerEventDefinition;
import org.flowable.engine.impl.bpmn.parser.BpmnParse;
import org.flowable.engine.impl.bpmn.parser.handler.AbstractBpmnParseHandler;

public class OneStartEventTimerParse extends AbstractBpmnParseHandler<StartEvent> {

    Engine bpmEngine;

    public OneStartEventTimerParse(Engine bpmEngine) {
        this.bpmEngine = bpmEngine;
    }

    @Override
    protected void executeParse(BpmnParse bpmnParse, StartEvent element) {
        //super.executeParse(bpmnParse, timerEventDefinition);

        for (EventDefinition definition : element.getEventDefinitions()) {
            if (definition instanceof TimerEventDefinition){
                bpmEngine.setTimerEventProcessesDeployed(true);
                break;
            }
        }



    }

    @Override
    protected Class<? extends BaseElement> getHandledType() {
        return StartEvent.class;
    }

}


