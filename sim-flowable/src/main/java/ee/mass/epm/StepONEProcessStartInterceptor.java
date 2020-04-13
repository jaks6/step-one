package ee.mass.epm;

import org.flowable.engine.interceptor.*;

import java.util.HashMap;
import java.util.Map;

/**
 * Attaches "localhost" process variable to every started process
 */
public class StepONEProcessStartInterceptor implements StartProcessInstanceInterceptor {

    private final Engine engine;

    public StepONEProcessStartInterceptor(Engine engine) {
        this.engine = engine;
    }

    @Override
    public void beforeStartProcessInstance(StartProcessInstanceBeforeContext instanceContext) {
        Map<String, Object> variables = instanceContext.getVariables();
        if (variables == null) variables = new HashMap<String, Object> ();
        variables.put("localhost", engine.getHostAddress());
        instanceContext.setVariables(variables);

    }

    @Override
    public void afterStartProcessInstance(StartProcessInstanceAfterContext instanceContext) {
        // not implemented
    }

    @Override
    public void beforeStartSubProcessInstance(StartSubProcessInstanceBeforeContext instanceContext) {
        // not implemented
    }

    @Override
    public void afterStartSubProcessInstance(StartSubProcessInstanceAfterContext instanceContext) {
        // not implemented
    }
}
