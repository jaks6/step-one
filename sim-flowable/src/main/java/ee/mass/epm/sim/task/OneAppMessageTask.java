package ee.mass.epm.sim.task;

import ee.mass.epm.sim.message.OneAppMessageContent;
import ee.mass.epm.sim.message.SimMessageContent;
import org.flowable.common.engine.api.delegate.Expression;
import org.flowable.engine.delegate.DelegateExecution;


// A message targeted to other ONE host applications, instead of the Flowable Engine.
public class OneAppMessageTask extends AbstractMessageTask {

    Expression app_id;

    @Override
    SimMessageContent getMessageContent(DelegateExecution execution) {
        OneAppMessageContent messageContent = new OneAppMessageContent();
        messageContent.appId = (String) app_id.getValue(execution);
        return messageContent;
    }
}
