package routing.util;

import core.*;

public class DistanceAwareEnergyModel extends EnergyModel {
    private double lastUpdate;
    private ModuleCommunicationBus comBus;
    private double currentEnergy;
    private double transmitEnergy;
    private double scanEnergy;

    public DistanceAwareEnergyModel(Settings s) {
        super(s);
    }

    /**
     * Reduces the energy reserve for the amount that is used by sending data
     * and scanning for the other nodes.
     */
    public void update(NetworkInterface iface, ModuleCommunicationBus comBus) {
        double simTime = SimClock.getTime();
        double delta = simTime - this.lastUpdate;

        if (this.comBus == null) {
            this.comBus = comBus;
            this.comBus.addProperty(ENERGY_VALUE_ID, this.currentEnergy);
            this.comBus.subscribe(ENERGY_VALUE_ID, this);
        }

        double maxDistance = Double.NEGATIVE_INFINITY;
        double distanceModifier = 1;
        if (simTime > this.lastUpdate) {
            /** apply distance modifier based on the furthest connection */
            for (Connection c : iface.getConnections()) {
                if (c.isTransferring()) {             /* sending or receiving data */

                    Coord remoteLocation = c.getOtherInterface(iface).getLocation();
                    double distance = remoteLocation.distance(iface.getLocation());

                    if (distance >= maxDistance) maxDistance = distance;
                }
            }
            //distance modifier is on a scale from [1, 2], linearly dependent from distance to other node.
            // modifier is 1 if distance in connection is 0,
            // modifier is 2 if distance is equal to maximum transmit range
            if (maxDistance != Double.NEGATIVE_INFINITY);
                distanceModifier = 1 + (maxDistance / iface.getTransmitRange());
            reduceEnergy(distanceModifier * delta * this.transmitEnergy);
        }
        this.lastUpdate = simTime;

        if (iface.isScanning()) {
            /* scanning at this update round */
            if (iface.getTransmitRange() > 0) {
                if (delta < 1) {
                    reduceEnergy(this.scanEnergy * delta);
                } else {
                    reduceEnergy(this.scanEnergy);
                }
            }
        }
    }
}
