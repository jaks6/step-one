package ee.mass.epm.sim.message;

public class SimMessage {

    // for one sim outgoing messages
    public String name;
    public int destinationAddress;
    public int originAddress;


    public String srcExecutionId;
    public int size;
    public boolean notifySourceOfDelivery = false; //whether the sending task should be notified on

    SimMessageContent content;

    @Override
    public String toString() {
        return "MSG: [" + name + "; dest: " + destinationAddress + "]";
    }


    public SimMessage(String name, int destinationAddress, int size) {
        this.name = name;
        this.destinationAddress = destinationAddress;
        this.size = size;
    }



    public void setContent(SimMessageContent content) {
        this.content = content;
    }

    public SimMessageContent getContent() {
        return content;
    }

    public void setOriginAddress(int originAddress) {
        this.originAddress = originAddress;
    }

    public void setSrcExecutionId(String srcExecutionId) {
        this.srcExecutionId = srcExecutionId;
    }

}
