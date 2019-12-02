package ee.mass.epm;

import org.flowable.engine.delegate.DelegateExecution;

public class FogMessageRequest {
    private final String processInstanceId;

    private final int processTaskSize;



    private final int qualityOfService;
    private final int timeout;

    public FogMessageRequest(DelegateExecution execution, int qos, int timeout) {

        this.processInstanceId = execution.getProcessInstanceId();

        this.processTaskSize = Integer.parseInt((String) execution.getVariable("processTaskSize")); //TODO
//        this.messageSize = Integer.parseInt((String) execution.getVariable("fogMsgSize")); //TODO
        this.qualityOfService = qos;
        this.timeout = timeout;
    }

    public String getProcessInstanceId() {
        return processInstanceId;
    }



    public int getProcessTaskSize() {
        return processTaskSize;
    }


    public int getQualityOfService() {
        return qualityOfService;
    }

    public int getTimeout() {
        return timeout;
    }

}
