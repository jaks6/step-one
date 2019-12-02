package ee.mass.epm.sim.task;

import ee.mass.epm.SimulatedProcessEngineConfiguration;
import ee.mass.epm.sim.message.SimMessage;
import ee.mass.epm.sim.message.SimMessageContent;
import org.flowable.bpmn.model.ServiceTask;
import org.flowable.common.engine.api.delegate.Expression;
import org.flowable.engine.delegate.DelegateExecution;
import org.flowable.engine.delegate.JavaDelegate;
import org.flowable.engine.impl.context.Context;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

public abstract class AbstractMessageTask implements JavaDelegate {
    public static final int DEFAULT_MSG_SIZE = 1;
    public static final int DEFAULT_DESTINATION_ADDRESS = -1;

    Expression msg_name;
    Expression msg_size;
    Expression msg_destination;

    Logger log = LoggerFactory.getLogger(this.getClass());


    @Override
    public void execute(DelegateExecution execution) {

        SimulatedProcessEngineConfiguration engine = (SimulatedProcessEngineConfiguration) Context.getProcessEngineConfiguration();
//        System.out.println("started execution = [" + execution + "; "+ execution.getCurrentFlowElement().getName() +  " engine: "+ engine+"]" );
        SimMessage msg = getSimMessage(execution);
        //if the task has attribute flowable:triggerable, the task doesnt finish until a trigger is sent to this task
        msg.notifySourceOfDelivery = ((ServiceTask) execution.getCurrentFlowElement()).isTriggerable();
        msg.setContent(getMessageContent(execution));

        engine.getOutgoingMessages().add(msg);
    }

    abstract SimMessageContent getMessageContent(DelegateExecution execution);



    SimMessage getSimMessage(DelegateExecution execution) {
        SimMessage msg;

        try {
//            int dest = getIntegerValueFromProcess(execution, msg_destination, destinationAddressVariable);
            int dest = getIntegerFromFieldExpression(execution, msg_destination);
            int messageSize = getIntegerFromFieldExpression(execution, msg_size);
            String messageName = (String) msg_name.getValue(execution);

            msg = new SimMessage(messageName, dest, messageSize);
            msg.setSrcExecutionId(execution.getId());
        } catch (Exception e) {
            log.error(e.getMessage());
            e.printStackTrace();
            return null;
        }
        return msg;
    }



    /**
     * Extracts a value from one of two provided Field Expressions. If both valueFieldExpression and variableFieldExpression are set, valueFieldExpression is used!
     *
     * @param execution
     * @param valueFieldExpression - if set, directly reads value from this field
     * @return
     * @throws Exception
     */
//    Object getValueFromProcess(DelegateExecution execution, Expression valueFieldExpression, Expression variableFieldExpression) throws Exception {
//        Object value = null;
//
//        if (valueFieldExpression == null && variableFieldExpression == null){
//            log.debug("MessageTask: "+ execution.getCurrentActivityId()+ ". Both value and variable were null (undefined!");
//            return null;
//        }
//
//
//        if (valueFieldExpression != null) {
//            value = valueFieldExpression.getExpressionText();
//        }
//
//        if (variableFieldExpression != null) {
//            if (valueFieldExpression == null){
//                log.debug("Set val from process variable");
//                value = execution.getVariable(variableFieldExpression.getExpressionText());
//                if (value == null){
//                    throw new Exception("MessageTask "+ execution.getCurrentActivityId()+" expected Process Variable " + variableFieldExpression.getExpressionText() + " to be set, but it was null!");
//                }
//            } else {
//                log.debug("Ignoring value set process-variable-expression, using expression directly");
//            }
//        }
//        return value;
//    }

    Object getObjectFromFieldExpression(DelegateExecution execution, Expression valueFieldExpression) throws Exception {
        if (valueFieldExpression != null){
            return valueFieldExpression.getValue(execution);
        } else {
            throw new Exception("Field expression undefined!");
        }
    }

    int getIntegerFromFieldExpression(DelegateExecution execution, Expression valueFieldExpression) throws Exception {
        Object objectFromFieldExpression = getObjectFromFieldExpression(execution, valueFieldExpression);
        return Integer.valueOf(objectFromFieldExpression.toString());
    }

    String getStringFromFieldExpression(DelegateExecution execution, Expression valueFieldExpression) throws Exception {
        Object objectFromFieldExpression = getObjectFromFieldExpression(execution, valueFieldExpression);
        return objectFromFieldExpression.toString();
    }

//    int getIntegerValueFromProcess(DelegateExecution execution, Expression valueFieldExpression, Expression variableFieldExpression) throws Exception {
//        Object value = getValueFromProcess(execution, valueFieldExpression, variableFieldExpression);
//
//        if (value instanceof String){
//            return Integer.valueOf((String) value);
//        } else if (value instanceof Integer){
//            return (int) value;
//        } else {
//            throw new Exception("Unsupported object type. destination address has to be String or Int");
//        }
//    }
}
