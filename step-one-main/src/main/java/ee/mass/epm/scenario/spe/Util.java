package ee.mass.epm.scenario.spe;

import org.apache.commons.lang3.Validate;

import java.util.HashSet;
import java.util.Random;

public class Util {

    //9 seemed to produce something past 30k mark
    private static final Random RANDOM = new Random(10); // TODO: support random seeds

    /** Return n unique numbers in range [low...high]
     * returns null if over 9999 attempts have been made to select unique numbers
     * @return*/
    public static HashSet<Integer> uniqueRandomIntList(int size, int low, int high){

         int i = 0;
        HashSet<Integer> result = new HashSet<>(size);




        if (size == 0 ) return result;
        while (result.size() < size){
            i++;
            result.add(nextInt(low, high));
            if (i > 9999){
                return null;
            }
        }

        return result;
    }


    /** From Apache.commons.lang3.RandomUtils.. We are redefining it here because we want to use our own random seeds */
    public static int nextInt(final int startInclusive, final int endExclusive) {
        Validate.isTrue(endExclusive >= startInclusive,
                "Start value must be smaller or equal to end value.");
        Validate.isTrue(startInclusive >= 0, "Both range values must be non-negative.");

        if (startInclusive == endExclusive) {
            return startInclusive;
        }

        return startInclusive + RANDOM.nextInt(endExclusive - startInclusive);
    }

}
