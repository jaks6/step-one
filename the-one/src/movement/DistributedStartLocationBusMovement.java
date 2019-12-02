package movement;

import core.Coord;
import core.Settings;
import core.SettingsError;

public class DistributedStartLocationBusMovement extends BusMovement {


    /**
     * Whether starting locations should be distributed as distantly as possible between stops.
     * Off by default (random first stop and subsequent ones follow sequentially)
     */
    public static final String ROUTE_DISTRIB_HOST_COUNT = "distributedStartingLocationHostCount";
    private static int distributionHostCounter = 0;
    private int distributionStep;
    private int distributedStartingLocationHostCount;
    public DistributedStartLocationBusMovement(Settings settings) {
        super(settings);


        if (settings.contains(ROUTE_DISTRIB_HOST_COUNT)){
            this.distributedStartingLocationHostCount = settings.getInt(ROUTE_DISTRIB_HOST_COUNT);
            if (distributedStartingLocationHostCount > getRoute().getNrofStops()){
                throw new SettingsError("Trying to distribute more hosts than there are stops!");
            }
            this.distributionStep = (int) Math.floor(getRoute().getNrofStops() / distributedStartingLocationHostCount);
            distributionHostCounter = 0;
        } else {
            throw new SettingsError("Must specify distributedStartingLocationHostCount to use DistributedStartLocationBusMovement!");
        }
    }

    /**
     * Copyconstructor. Gives a route to the new movement model from the
     * list of routes and randomizes the starting position.
     * @param proto The MapRouteMovement prototype
     */
    protected DistributedStartLocationBusMovement(BusMovement proto) {
        super(proto);

    }

    @Override
    public DistributedStartLocationBusMovement replicate() {
        DistributedStartLocationBusMovement replicate = new DistributedStartLocationBusMovement(this);
        replicate.distributionStep = this.distributionStep;
        return replicate;
    }



    /**
     * Returns the first stop on the route
     */
    @Override
    public Coord getInitialLocation() {


        getRoute().setNextIndex(distributionHostCounter * distributionStep);
        distributionHostCounter++;
        if (lastMapNode == null) {
            lastMapNode = getRoute().nextStop();
        }

        return lastMapNode.getLocation().clone();
    }

}
