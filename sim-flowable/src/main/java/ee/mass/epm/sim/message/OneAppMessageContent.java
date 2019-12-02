package ee.mass.epm.sim.message;

public class OneAppMessageContent implements SimMessageContent {

    public String appId;
    @Override
    public SimMessageType getMessageContentType() {
        return SimMessageType.ONE_MSG;
    }
}
