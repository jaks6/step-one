package ee.mass.epm.sim.task;

import ee.mass.epm.sim.message.EngineMessageContent;
import ee.mass.epm.sim.message.SimMessageContent;
import org.apache.commons.lang3.StringUtils;
import org.flowable.engine.delegate.DelegateExecution;

/**
 * This task should be used for messages which are meant to start new process instances
 */
public class StartEventMessageTask extends MessageTask {

    @Override
    SimMessageContent getMessageContent(DelegateExecution execution) {
        EngineMessageContent msgContent = new EngineMessageContent();

        msgContent.isForStartEvent = true;

        msgContent.variables.put(this.msg_name + "_execution_id", execution.getId()); // TODO: maybe move to engine-middleware level instead of this task impl.

        if (included_vars != null) {
            String[] varNames = included_vars.getExpressionText().split(MessageTask.INCLUDED_PROCESS_VARS_DELIMITER);
            msgContent.addProcessVarsFromExecution(execution, varNames);
        }
        return msgContent;
    }
}
