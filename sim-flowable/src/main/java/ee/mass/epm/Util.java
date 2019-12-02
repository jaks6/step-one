package ee.mass.epm;

import core.Coord;
import org.apache.commons.lang3.StringUtils;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

public class Util {


    static Logger log = LoggerFactory.getLogger(Util.class);
    /**
     *
     * Valid formats for coordinate are (whitespace is ignored):
     * 1) (123.0, 232.0) - this format is toString() of core.Coord of ONE sim
     * 2) 123.0, 232.0
     * 3) (123.0; 232.0)
     * 4) 123.0; 232.0
     */

    public static Coord extractCoordFromString(String coordString){
        if (coordString== null){
            log.error("argument coordString was null!");
            return null;
        }
        coordString = coordString.replaceAll("\\(","").replaceAll("\\)","");
        String[] strings = StringUtils.stripAll(coordString.split(","));

        if (strings.length != 2){
            strings = StringUtils.stripAll(coordString.split(";"));
        }

        if (strings.length != 2){
            log.error("Failed to parse Coord from field expression!");
        }
        return new Coord(Double.valueOf(strings[0]), Double.valueOf(strings[1] ));

    }
}
