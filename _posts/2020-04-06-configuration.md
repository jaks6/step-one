---
layout: paginated_post
title:  "Configuring STEP-ONE"
date:   2020-04-06 11:35:32 +0300
categories: other
---
# ONE Settings files

ONE simulations can use 2 configuration files:

1. Default settings, called "default_settings.txt", this is shared across different simulation scenarios

2. A settings file passed to the ONE simulator program as an argument when running a specific simulation. This settings file can override some settings set in the default_settings.txt or introduce new ones. 

   1. e.g. for *tutorial 1*, we have a settings.txt in the tutorial1_basic_messaging folder.

   The original guidelines for configuring ONE can be [found here](https://github.com/akeranen/the-one/wiki/README#configuring). In this guide, we  take a look at the *default_settings.txt* used for STEP-ONE tutorials that is shared between all of them. The file specifies various aspects of the simulation:

* The name, duration of the simulation
  
  ```
  ## Scenario settings
  Scenario.name = Tutorial
  Scenario.simulateConnections = true
  Scenario.updateInterval = 1.0
  Scenario.endTime = 3600
  
  Scenario.nrofHostGroups = 2
  ```
  
  * Here the simulation should run for 3600 seconds with a timestep of 1s
  
  * There are 2 groups of hosts. Host groups allow to specify different behaviour and attributes for hosts, e.g. their movement behaviour, the applications & processes they support, their networking capabilities. 
  
    
  
* Creating of communication/networking interfaces
  
  ```
  # "Bluetooth" interface for all nodes
  btInterface.type = SimpleBroadcastInterface
  btInterface.transmitSpeed = 250k
  btInterface.transmitRange = 12
  
  # assign network interface to all groups
  Group.nrofInterfaces = 1
  Group.interface1 = btInterface
  ```
  
  * In this example, an interface of type "SimpleBroadcastInterface" is created and named *btInterface*. It is configured to have a range of 12 meters and a fixed transmitspeed of 250kBps.
  
  * Each host group has exaxctly 1 network interface, *btInterface*
  
    
  
* Configuration of message routing

  ```
  ## Message Router
  Group.router = DirectDeliveryRouter
  Group.bufferSize = 5M
  deleteDelivered = true;
  Group.msgTtl = 99999
  ```

  * Set up how messages are routed over network interfaces. DirectDeliveryRouter will send a message if a host has a direct connection to the message's destination. Messages are configured to have a long time-to-live (TTL) time and all host groups are set to have a message buffer of 5Mbytes.

    * Note: ONE includes different routing schemes for opportunistic networks where a direct connection is not needed.

      

* Host groups are declared. Host groups are an important ONE simulator concept that allow assigning hosts to groups and defining separate behaviour for each group. For example, you could:

  * create a group for servers and another group for clients and specify that servers should not move, while clients should move
  * specify that all host groups should have a process engine, but each host group should have a different set of processes running on their engine

  ```
  Group.nrofHosts = 1
  Group1.groupID = A
  Group2.groupID = B
  
  # Assign same movement model to all node groups, but
  # specify different coordinates for Group1, Group2
  Group.movementModel = StationaryMovement
  
  Group1.nodeLocation = 200,200
  Group2.nodeLocation = 200,210
  
  ```

  * Here, each host group is set to have 1 host, the groups are named A  and B. All groups will have *StationaryMovement* mobility model, which means the hosts are not moving. The groups are assigned different locations on the map via coordinates.

## Configuring Process Engines and Processes

ONE hosts can run applications - applications can receive and produce messages. STEP-ONE provides a special kind of application: a process engine.

To use STEP-ONE process management, a host (group) needs to embed the application *bpm.BpmEngineApplication*

In the below example, there are 2 different configurations for the *BpmEngineApplication*:



```
# Declare 2 applications, called processEngineApp1 and processEngineApp2,
# both of type bpm.BpmEngineApplication. They will be configured to run different processes on A and B
processEngineAppA.type = bpm.BpmEngineApplication
processEngineAppB.type = bpm.BpmEngineApplication

# For all host groups, say that they embed exactly 1 application
Group.nrofApplications = 1

# Now specify separately for host group which process engine variant they should embed
Group1.application1 = processEngineAppA
Group2.application1 = processEngineAppB

# Configure the engine variants:

# Set some process definitions to be auto-deployed to the engines once the simulation starts
processEngineAppA.autoDeployedProcesses = samples/tutorial1_basic_messaging/Tutorial_1_Basic_Messaging_B.bpmn20

# Set a process to be auto-started
processEngineApp1.autoStartedProcessKeys = tutorial1_a

```

## Other notes



A very useful feature of ONE settings is the [run indexing feature](https://github.com/akeranen/the-one/wiki/README#run-indexing) .