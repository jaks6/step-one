---
layout: paginated_post
title:  "Example: Cloud vs. Fog "
date:   2020-04-07 13:00:32 +0300
permalink: examples/cloud-vs-fog
categories: examples
---

## Introduction
This example scenario shows how a mobile node can dynamically decide where to offload a process to, based on:

* Whether any fog nodes are currently available (connected)
* Information about connected fog nodes' current CPU load (acquired through message exchange)

If a fog node is available and the load is not above a threshold, the mobile node will offload work to the fog node. Otherwise, it will send the work to a cloud node.

The decision-making based on CPU load information is implemented as a custom Java Service Task.

The example also include process-related report generation to get an overview of which host ran how many processes, when were processes started, finished, etc.

## Processes

There are 3 process definitions:

1. **<u>Main Process</u>** - run by mobile node. 

   * Starts with a repeating start timer event (starts every 30s for a total of 10 times).
   * Sets a process variable workSize with a script task
   * Uses A custom Java subtask of SimulatedTask to try and get a connected fog servers address, sets it to variable fogServerAddress
   * XOR gateway checks if var *fogServerAddress* is set. 
     * If not, then for the negative flow (Use Cloud), an execution listener will set the *workerAddress* variable to 2 (2 corresponds to address of cloud in this scenario).
     * Alternatively, do a message exchange with the fog server asking for a load report. Upon receiving the report, use anotehr custom Java implementation to analyze the load report and decide whether to use the fog server
   * Deploy the "worker process" and start a new instance of it.

   ![](https://kodu.ut.ee/~jaks/public/step-one/wiki-media/cloudvsfog/process_main.png)

2. **<u>Load Report process</u>** Pre-deployed to fog nodes, this process reacts to Load Inquiry messages, gets information about CPU and its load, assigns the info to variables and sends the variables as a Load Report message.

   ![](https://kodu.ut.ee/~jaks/public/step-one/wiki-media/cloudvsfog/process_loadreport.png)

3. **<u>Worker Process</u>** represents the compute task which Main Process wants to offload to either fog or cloud. Main Process takes care of deploying it and starting it.

   ![](https://kodu.ut.ee/~jaks/public/step-one/wiki-media/cloudvsfog/process_worker.png)

   

## Checking for Fog Servers in Java

The *"Check if connected to Fog Servers"* Task in the Main Process is implemented as follows:

​	 *step-one/step-one-main/src/main/java/ee/mass/epm/samples/CheckConnectedFogServers.java* 

```java
package ee.mass.epm.samples;

import ...

public class CheckConnectedFogServers extends SimulatedTask {
    @Override
    public void execute(DelegateExecution execution) {
        super.execute(execution);

        List<DTNHost> hostList = SimScenario.getInstance().getHosts();

        // localhost is the one executing this task:
        DTNHost localhost = hostList.get(execution.getVariable("localhost", Integer.class));


        // Find all connected nodes with name starting with "Fog" and 
        // choose the onewith fastest connection
        Optional<Connection> fastestConnection = localhost.getConnections().stream()
                .filter(c -> c.isUp() && c.getOtherNode(localhost).getName().startsWith("Fog"))
                .max(Comparator.comparing(Connection::getSpeed));

        if (fastestConnection.isPresent()){
            int fogAddress = fastestConnection.get().getOtherNode(localhost).getAddress();
            execution.setVariable("fogServerAddress", fogAddress);
        }

    }
}
```



## Decision-making in Java

The "Choose worker based on Fog load" task of Main Process is implemented as:

 *step-one/step-one-main/src/main/java/ee/mass/epm/samples/CheckConnectedFogServers.java* 

```java
package ee.mass.epm.samples;

import ...

import java.util.List;

public class DecideWorkerTask  extends SimulatedTask {
    private static final double QUEUE_TIME_THRESHOLD = 100; // seconds. consider using a process variable

    /**
     * Decide whether to use Fog or Cloud based on information about Fog hosts' CPU configuration,
     * the size of CPU job queue of the Fog, and the size of the job item we want to add to the Fog.
     * If the the estimated waiting time in queue is below a theshold, use the fog, otherwise use cloud.
     */
    @Override
    public void execute(DelegateExecution execution) {
        super.execute(execution);

        // Information we received from the fog node previously:
        int reportedLoad = execution.getVariable("queueSizeMips", Integer.class);
        int cpuSpeed = execution.getVariable("cpuSpeed", Integer.class);
        int totalCpus = execution.getVariable("noOfCpus", Integer.class);

        // information about the job we want to add:
        int workSize = execution.getVariable("workSize", Integer.class);


        // We can estimate no. of jobs on server if we assume all jobs on server have the same size (workSize) for this example
        double estimateNoOfWorksInQueue = Math.ceil(reportedLoad / (double) workSize);

        int activeCPUs = Math.min ( (int) estimateNoOfWorksInQueue, totalCpus);

        // how long to finish my new job:
        double estimatedTimeToMyWork = workSize / (double) cpuSpeed;

        double estimatedTotalWaitingTime;
        if (activeCPUs < totalCpus) {
            estimatedTotalWaitingTime = estimatedTimeToMyWork;
        } else {
            double estimatedTimeToFinishQueue = reportedLoad / (double) ( cpuSpeed * activeCPUs);
            estimatedTotalWaitingTime = estimatedTimeToFinishQueue + estimatedTimeToMyWork;
        }

        List<DTNHost> hostList = SimScenario.getInstance().getHosts();

        int fogServerAddress = execution.getVariable("fogServerAddress", Integer.class);

        DTNHost cloud = hostList.stream().filter(dtnHost -> dtnHost.getName().startsWith("Cloud")).findFirst().orElse(null);

        if ( estimatedTotalWaitingTime <= QUEUE_TIME_THRESHOLD ) {
            execution.setVariable("workerAddress", fogServerAddress);
        } else {
            execution.setVariable("workerAddress", cloud.getAddress());
        }

    }
}
```





## Running the scenario: Reports

When you run the simulation, the first iterations of main process offload work to the fog, until the fog load exceeds the configured threshold, after which the cloud is utilized. If the fogs load decreases, it is utilized again.



This example is configured to generate 2 report files:

![](https://kodu.ut.ee/~jaks/public/step-one/wiki-media/cloudvsfog/report_files.png)

For example, the *HostBpmReport* has the following  csv-structured content:

```
hostname;proc_started;proc_completed;proc_cancelled;act_started;act_completed;msgs_sent;msgs_recvd;signals_recvd
Cloud2;4;4;0;12;16;4;8;0
Fog1;16;16;0;48;64;16;22;1
Mobile0;10;10;0;100;110;30;20;1
```

the "*proc_started*" column represents number of processes started, and similarly "*proc_completed*" is for completed processes.

We can see that the host Mobile0 started a total of 10 processes, all of them got completed. We see that out of those 10, 4 iterations used Cloud as the worker, since Cloud has run 4 processes during the simulation.

### Full BPM report

Also, we can get an idea of the timeline of this scenario if we look at the "FullBpmReport":

![](https://kodu.ut.ee/~jaks/public/step-one/wiki-media/cloudvsfog/report_csv.png)

The above image has sorted the entries based on the their start time (proc_started column).

As we can see, the Fog server is chosen as the worker 2 times before the cloud is first used for the 3rd iteration (proc_started 96, line 11). After which, the Fog has become idle again and will be used.

Looking at the totaltime column, we can see that as time increases, the overlap time between running multiple workers on the fog is increasing, and this results in the totaltime of the worker becoming larger in the later iterations.