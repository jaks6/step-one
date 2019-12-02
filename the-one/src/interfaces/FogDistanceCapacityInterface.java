package interfaces;

import core.NetworkInterface;
import core.Settings;

import java.util.Arrays;

public class FogDistanceCapacityInterface extends DistanceCapacityInterface {
    private int maxSpeed;

    public FogDistanceCapacityInterface(Settings s) {
        super(s);
        maxSpeed = Arrays.stream(this.transmitSpeeds).max().getAsInt();
    }

    public FogDistanceCapacityInterface(FogDistanceCapacityInterface ni) {
        super(ni);
        this.maxSpeed = ni.maxSpeed;
    }

    public int getMaxTransmitSpeed() {
        return this.maxSpeed;

    }

    @Override
    public NetworkInterface replicate()	{
        return new FogDistanceCapacityInterface(this);
    }


    /** Adapted from @DistanceCapacityInterface.java::getTransmitSpeed() */
    public int getMeanTransmitSpeed(double meanDistance) {
        double fractionIndex;
        double decimal;
        double speed;
        int index;

        if (meanDistance >= this.transmitRange) {
            return 0;
        }

        /* interpolate between the two speeds */
        fractionIndex = (meanDistance / this.transmitRange) *
                (this.transmitSpeeds.length - 1);
        index = (int)(fractionIndex);
        decimal = fractionIndex - index;

        speed = this.transmitSpeeds[index] * (1-decimal) +
                this.transmitSpeeds[index + 1] * decimal;

        return (int)speed;
    }
}
