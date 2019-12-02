package report;


import applications.bpm.BpmEngineApplication;
import core.Application;
import core.ApplicationListener;
import core.DTNHost;

import java.util.HashMap;

public class DetailedBpmReporter extends Report implements ApplicationListener {

    public static final String PROCESS_STARTED = "process_start";
    public static final String PROCESS_COMPLETED = "process_completed";
    public static final String PROCESS_CANCELLED = "process_cancelled";
    public static final String ACTIVITY_COMPLETED = "activity_completed";
    public static final String ACTIVITY_STARTED = "activity_started";
    public static final String ACTIVITY_CANCELLED = "activity_cancelled";
    public static final String TASK_CREATED = "task_created";
    public static final String TASK_COMPLETED = "task_completed";
    public static final String SENT_MESSAGE = "sentBpmMessage";
    public static final String RECEIVED_MESSAGE = "receivedBpmMessage";
    public static final String RECEIVED_SIGNAL_NEARBY = "signalNearbyDevice";

    HashMap<DTNHost, HashMap> hostStatsMap;

    public DetailedBpmReporter() {
        hostStatsMap = new HashMap<>();
    }

    public void gotEvent(String event, Object params, Application app,
                         DTNHost host) {
        // Check that the event is sent by correct application type
        if (!(app instanceof BpmEngineApplication)) return;
        if (host == null) return;
        HashMap<String, Integer> statsMap = hostStatsMap.getOrDefault(host, new HashMap<String, Integer>());


        int count = statsMap.getOrDefault(event, 0);
        statsMap.put(event, count + 1);

        hostStatsMap.put(host, statsMap);



    }

    @Override
    public void done() {
//        write("Bpm stats for scenario " + getScenarioName() +
//                "\nsim_time: " + format(getSimTime()));
        String header = "hostname;proc_started;proc_completed;proc_cancelled;act_started;act_completed;msgs_sent;msgs_recvd;signals_recvd";
        write(header);


        hostStatsMap.forEach((host, hostStats) -> {
            String csvLine = String.join(";",
                    host.getName(),
                    String.valueOf( hostStats.getOrDefault(PROCESS_STARTED, 0)),
                    String.valueOf( hostStats.getOrDefault(PROCESS_COMPLETED,0)),
                    String.valueOf( hostStats.getOrDefault(PROCESS_CANCELLED,0)),
                    String.valueOf( hostStats.getOrDefault(ACTIVITY_STARTED,0)),
                    String.valueOf( hostStats.getOrDefault(ACTIVITY_COMPLETED,0)),
                    String.valueOf( hostStats.getOrDefault(SENT_MESSAGE,0)),
                    String.valueOf( hostStats.getOrDefault(RECEIVED_MESSAGE,0)),
                    String.valueOf( hostStats.getOrDefault(RECEIVED_SIGNAL_NEARBY,0))
            );

            write(csvLine);


//            String statsText = "Processes started: " + hostStats.get(PROCESS_STARTED) +
//                    "\nProcesses completed: " + hostStats.get(PROCESS_COMPLETED)+
//
//                    "\nActivities started: " + hostStats.get(ACTIVITY_STARTED)+
//                    "\nActivities completed: " + hostStats.get(ACTIVITY_COMPLETED)+
//
//                    "\nMessages sent: " + hostStats.get(SENT_MESSAGE)+
//                    "\nMessages received: " + hostStats.get(RECEIVED_MESSAGE)+
//
//                    "\nSignals received: " + hostStats.get(RECEIVED_SIGNAL_NEARBY)
//
//                    ;
//
//            write(statsText);

        });


        super.done();
    }
}
