package applications.bpm;

import core.Coord;
import core.DTNHost;
import org.apache.commons.lang3.StringUtils;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

import java.io.File;
import java.io.IOException;
import java.nio.charset.Charset;
import java.nio.charset.StandardCharsets;
import java.nio.file.Files;
import java.nio.file.Paths;
import java.util.ArrayList;
import java.util.HashMap;


public class Util {
    public static HashMap<String, String> PROCESSES =  new HashMap<>();

    public static String stringFromFile(String s) throws IOException {
        File process = new File(s);
        String string = readFile(process.getPath(), StandardCharsets.UTF_8);
        return string;
    }

    public static String getCachedProcess(String s) throws IOException {
        if (!PROCESSES.containsKey(s)) PROCESSES.put(s, readFile(s, StandardCharsets.UTF_8));

        return PROCESSES.get(s);
    }

    static String readFile(String path, Charset encoding) throws IOException {
        byte[] encoded = Files.readAllBytes(Paths.get(path));
        return new String(encoded, encoding);
    }

    /** Gets the arraylist stored with the given key, or returns an empty arraylist if
     * no value attached to the key.
     * @param host
     * @param key
     * @return
     */
    public static <T> ArrayList<T> getArrayListFromComBus(DTNHost host, String key){
        if (host.getComBus().containsProperty(key)){
//            return (ArrayList<T>) host.getComBus().getProperty(COMBUS_PENDING_FOG_REQS);
            return new ArrayList<T>();

        } else {
            return new ArrayList<T>();
        }
    }

    static Logger log = LoggerFactory.getLogger(Util.class);
    /**
     *
     * Valid formats for coordinate are (whitespace is ignored):
     * 1) (123.0, 232.0) - this format is toString() of core.Coord of ONE sim
     * 2) 123.0, 232.0
     * 3) (123.0; 232.0)
     * 4) 123.0; 232.0
     */
    // TODO: this method has duplicate in other module

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
