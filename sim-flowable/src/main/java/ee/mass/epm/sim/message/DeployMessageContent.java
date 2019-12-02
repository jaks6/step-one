package ee.mass.epm.sim.message;

/**
 * This message is consumed at the Process Management Application level.
 */
public class DeployMessageContent implements SimMessageContent {


    public String resourcePath;

    @Override
    public SimMessageType getMessageContentType() {
        return SimMessageType.DEPLOY_MSG;
    }
}
