package ee.mass.epm.sim;

import ee.mass.epm.Engine;
import org.flowable.bpmn.model.TimerEventDefinition;
import org.flowable.engine.impl.bpmn.parser.BpmnParse;
import org.flowable.engine.impl.bpmn.parser.handler.TimerEventDefinitionParseHandler;

public class OneTimerParse extends TimerEventDefinitionParseHandler {

    Engine bpmEngine;

    public OneTimerParse(Engine bpmEngine) {
        this.bpmEngine = bpmEngine;
    }

    @Override
    protected void executeParse(BpmnParse bpmnParse, TimerEventDefinition timerEventDefinition) {
        //super.executeParse(bpmnParse, timerEventDefinition);

        bpmEngine.setTimerEventProcessesDeployed(true);


    }
}


