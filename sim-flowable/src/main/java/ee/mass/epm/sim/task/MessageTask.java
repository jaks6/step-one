package ee.mass.epm.sim.task;

import ee.mass.epm.sim.message.EngineMessageContent;
import ee.mass.epm.sim.message.SimMessageContent;
import org.apache.commons.lang3.StringUtils;
import org.flowable.common.engine.api.delegate.Expression;
import org.flowable.engine.delegate.DelegateExecution;

// A message targeted to the Flowable Engine.
public class MessageTask extends AbstractMessageTask {

    static final String INCLUDED_PROCESS_VARS_DELIMITER = ",";

    Expression target_process_instance_id;
    Expression included_vars;


    @Override
    SimMessageContent getMessageContent(DelegateExecution execution) {
        // TODO: consider refactoring all of this logic under the MessageContent class itself
        EngineMessageContent msgContent = new EngineMessageContent();

        try {
            if (target_process_instance_id != null){ //todo should we allow generic messages?
                String destinationExecutionId = getStringFromFieldExpression(execution, target_process_instance_id);
                if (destinationExecutionId != null){
                    msgContent.destinationProcessInstanceId = destinationExecutionId;
                } else {
                    throw new Exception("Destination process instance ID was null!" + execution.getId() );
                }
            }
        } catch (Exception e) {
            e.printStackTrace();
        }

        msgContent.variables.put(this.msg_name.getValue(execution) + "_execution_id", execution.getId()); // TODO: maybe move to engine-middleware level instead of this task impl.

        if (included_vars != null) {
            String expressionText = included_vars.getExpressionText();
            String[] varNames = StringUtils.stripAll(expressionText.split(MessageTask.INCLUDED_PROCESS_VARS_DELIMITER));
            msgContent.addProcessVarsFromExecution(execution, varNames);
        }
        return msgContent;
    }


}
