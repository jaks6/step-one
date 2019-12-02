/*
 * Copyright 2010 Aalto University, ComNet
 * Released under GPLv3. See LICENSE.txt for details.
 */

package report;

import applications.bpm.BpmEngineApplication;
import core.Application;
import core.ApplicationListener;
import core.DTNHost;

import java.util.HashMap;

/**
 * Reporter for the <code>BpmEngineApplication</code>. Counts the number of pings
 * and pongs sent and received. Calculates success probabilities.
 *
 * @author jakob
 */
public class BpmAppReporter extends Report implements ApplicationListener {

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
    public static final String ENTITY_DELETED = "entityDeleted";
    public static final String ENTITY_CREATED = "entityCreated";

    HashMap<String, Integer> statsMap;
    HashMap<String, Integer> hostStatsMap;

    public BpmAppReporter() {
        statsMap = new HashMap<>();
        hostStatsMap = new HashMap<>();
    }

    public void gotEvent(String event, Object params, Application app,
                         DTNHost host) {
		// Check that the event is sent by correct application type
		if (!(app instanceof BpmEngineApplication)) return;

        int count = statsMap.containsKey(event) ? statsMap.get(event) : 0;
        statsMap.put(event, count + 1);






	}

	@Override
	public void done() {
		write("Bpm stats for scenario " + getScenarioName() +
				"\nsim_time: " + format(getSimTime()));

		String statsText = "Processes started: " + this.statsMap.get(PROCESS_STARTED) +
			"\nProcesses completed: " + this.statsMap.get(PROCESS_COMPLETED)+
			"\nProcesses cancelled: " + this.statsMap.get(PROCESS_CANCELLED)+

			"\nActivities started: " + this.statsMap.get(ACTIVITY_STARTED)+
			"\nActivities completed: " + this.statsMap.get(ACTIVITY_COMPLETED)+
			"\nActivities cancelled: " + this.statsMap.get(ACTIVITY_CANCELLED)+

            "\nMessages sent: " + this.statsMap.get(SENT_MESSAGE)+
			"\nMessages received: " + this.statsMap.get(RECEIVED_MESSAGE)+

            "\nTasks created: " + this.statsMap.get(TASK_CREATED)+
            "\nTasks completed: " + this.statsMap.get(TASK_COMPLETED)+

            "\n'Nearby' Signals received: " + this.statsMap.get(RECEIVED_SIGNAL_NEARBY) +
            "\nEntities created: " + this.statsMap.get(ENTITY_CREATED) +
            "\nEntities deleted: " + this.statsMap.get(ENTITY_DELETED)

                ;

		write(statsText);
		super.done();
	}
}
