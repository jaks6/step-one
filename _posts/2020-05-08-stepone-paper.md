---
layout: paginated_post
title:  "Smart City Road Monitoring"
date:   2020-05-05 13:00:32 +0300
permalink: examples/bottom-up-offloading
categories: examples
---

## Introduction
This scenario was presented in the paper:

* [Mass, Jakob, Satish Narayana Srirama, and Chii Chang. "STEP-ONE:  Simulated Testbed for Edge-Fog Processes based on the Opportunistic  Network Environment simulator." *Journal of Systems and Software* (2020): 110587.](https://doi.org/10.1016/j.jss.2020.110587)

A Central Road Monitoring Info system manages the monitoring of ***road segments*** in the city. A road segment starts and ends at a bus stop.

At a fixed interval, the road monitoring system schedules a process for each road segment:

* Capturing video footage of the road segment and analysing the video to identify road conditions (potholes, etc)

The video capture is performed by buses, but the video analysis is done either by Fog nodes (located at bus stops) or a Cloud node.



The paper presents the design and performance of this scenario with various configurations (e.g. comparing the use of fog or cloud, and performance of fog nodes depending on their geographic placement).



<div class="myvideo">
       <video  style="display:block; width:100%; height:auto;" autoplay controls loop="loop">
           <source src="https://kodu.ut.ee/~jaks/public/step-one/wiki-media/spe_scenario.webm" type="video/webm" />
       </video>
    </div>

The above video demonstrates the scenario running.

The green and purple lines demonstrate instances of the road segment analysis process. The colour reflects whether the processing has been assigned to a fog server (green) or the cloud (purple).





## Processes

An overview of the processes and their interactions is captured in this diagram:

![](https://kodu.ut.ee/~jaks/public/step-one/wiki-media/stepone_spe.jpg)