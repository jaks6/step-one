package ee.mass.epm.sim.message;

public interface SimMessageContent {

    enum SimMessageType { ENGINE_MSG, DEPLOY_MSG, ONE_MSG};

    SimMessageType getMessageContentType();
}
