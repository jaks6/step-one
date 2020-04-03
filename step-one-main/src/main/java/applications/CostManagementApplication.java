package applications;

import core.Application;
import core.DTNHost;
import core.Message;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

public class CostManagementApplication extends Application {


    Logger log = LoggerFactory.getLogger(this.getClass());
    public static final String APP_ID = "ee.mass.BusApp";

    // Copy-constructor
    public CostManagementApplication(CostManagementApplication a) { super(a);    }

    /** Unused, just return the message */
    @Override
    public Message handle(Message msg, DTNHost host) {
        return msg;
    }

    /**
     * Called every simulation cycle.
     *
     * @param host The host this application instance is attached to.
     */
    @Override
    public void update(DTNHost host) {

    }

    @Override
    public Application replicate() {
        return new CostManagementApplication(this);
    }
}
