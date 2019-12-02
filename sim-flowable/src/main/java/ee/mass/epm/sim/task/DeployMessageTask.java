package ee.mass.epm.sim.task;

import ee.mass.epm.sim.message.DeployMessageContent;
import ee.mass.epm.sim.message.SimMessageContent;
import org.flowable.common.engine.api.delegate.Expression;
import org.flowable.engine.delegate.DelegateExecution;

// TODO: add support for multiple resources at once
public class DeployMessageTask extends AbstractMessageTask {

    Expression deployed_resource;

    //TODO : considering adding resource name as required Var

    @Override
    SimMessageContent getMessageContent(DelegateExecution execution) {

        DeployMessageContent messageContent = new DeployMessageContent();
        try {
            messageContent.resourcePath = getStringFromFieldExpression(execution, deployed_resource);
        } catch (Exception e) {
            log.error(e.getMessage());
            e.printStackTrace();
            return null;
        }

        return messageContent;
    }

}
