package ee.mass.epm.stepone;

import core.DTNSim;
import core.Settings;
import ee.mass.epm.stepone.gui.StepOneDTNSimGUI;
import gui.DTNSimGUI;
import org.apache.commons.lang3.reflect.FieldUtils;
import ui.DTNSimTextUI;

import java.lang.reflect.InvocationTargetException;
import java.lang.reflect.Method;
import java.util.ArrayList;
import java.util.List;

public class StepOneSim extends DTNSim {

    /** List of class names that should be reset between batch runs */
    //private static List<Class<?>> resetList = new ArrayList<Class<?>>();

    /**
     * Starts the user interface with given arguments.
     * If first argument is {@link #BATCH_MODE_FLAG}, the batch mode and text UI
     * is started. The batch mode option must be followed by the number of runs,
     * or a with a combination of starting run and the number of runs,
     * delimited with a {@value #RANGE_DELIMETER}. Different settings from run
     * arrays are used for different runs (see
     * {@link Settings#setRunIndex(int)}). Following arguments are the settings
     * files for the simulation run (if any). For GUI mode, the number before
     * settings files (if given) is the run index to use for that run.
     * @param args Command line arguments
     */
    public static void main(String[] args) {
        Method parseNrofRuns = null;
        Method initSettings = null;
        try {
            parseNrofRuns = DTNSim.class.getDeclaredMethod("parseNrofRuns", String.class);
            initSettings = DTNSim.class.getDeclaredMethod("initSettings", String[].class, int.class);
            //TODO consider if using reflection like this should be remvoed
            parseNrofRuns.setAccessible(true);
            initSettings.setAccessible(true);
        } catch (NoSuchMethodException e) {
            e.printStackTrace();
        }


        boolean batchMode = false;
        int nrofRuns[] = {0,1};
        String confFiles[];
        int firstConfIndex = 0;
        int guiIndex = 0;

        /* set US locale to parse decimals in consistent way */
        java.util.Locale.setDefault(java.util.Locale.US);

        try {
            if (args.length > 0) {
                if (args[0].equals(BATCH_MODE_FLAG)) {
                    batchMode = true;
                    if (args.length == 1) {
                        firstConfIndex = 1;
                    }
                    else {

                        nrofRuns = (int[]) parseNrofRuns.invoke(null, args[1]);
//                    nrofRuns = parseNrofRuns(args[1]);
                        firstConfIndex = 2;
                    }
                }
                else { /* GUI mode */
                    try { /* is there a run index for the GUI mode ? */
                        guiIndex = Integer.parseInt(args[0]);
                        firstConfIndex = 1;
                    } catch (NumberFormatException e) {
                        firstConfIndex = 0;
                    }
                }
                confFiles = args;
            }
            else {
                confFiles = new String[] {null};
            }

//        initSettings(confFiles, firstConfIndex);
            initSettings.invoke(null, confFiles, firstConfIndex);

            if (batchMode) {
                long startTime = System.currentTimeMillis();
                for (int i=nrofRuns[0]; i<nrofRuns[1]; i++) {
                    System.out.println("Run " + (i+1) + "/" + nrofRuns[1]);
                    Settings.setRunIndex(i);
                    resetForNextRun();
                    new DTNSimTextUI().start();
                }
                double duration = (System.currentTimeMillis() - startTime)/1000.0;
                System.out.println("---\nAll done in " + String.format("%.2f", duration) + "s");
            }
            else {
                Settings.setRunIndex(guiIndex);
                new StepOneDTNSimGUI().start();
            }
        } catch (IllegalAccessException | InvocationTargetException e) {
            e.printStackTrace();
        }
    }

    /**
     * Resets all registered classes.
     */
    private static void resetForNextRun() {
        try {
            List<Class<?>> resetList = (List<Class<?>>) FieldUtils.readStaticField(DTNSim.class, "resetList", true);

        for (Class<?> c : resetList) {
            try {
                Method m = c.getMethod(RESET_METHOD_NAME);
                m.invoke(null);
            } catch (Exception e) {
                System.err.println("Failed to reset class " + c.getName());
                e.printStackTrace();
                System.exit(-1);
            }
        }
        } catch (IllegalAccessException e) {
            e.printStackTrace();
        }
    }




}
