## STEP-ONE simulator:
STEP-ONE is packaged as a *Gradle* project.

Run a specific simulation settings file with gradle *run*, for example:

`gradlew run --args samples/tutorial1_basic_messaging/settings.txt`



## STEP-ONE modeler:

To produce BPMN process models for STEP-ONE, use the STEP-ONE Flowable Modeller web application:

* Install [Docker](https://docs.docker.com/engine/install/) and [Docker Compose](https://docs.docker.com/compose/install/)
* Go to *step-one-modeler* directory, which contains the *docker-compose.yml* file.  `cd ./step-one/step-one-modeler`
* Run `docker-compose up`
* After the web-app service has finished initialising, use a browser to open the modeller at:
  http://localhost:8888/flowable-modeler

  

## Documentation:
See the [examples and tutorials page](https://jaks6.github.io/step-one/).


## Publications

Please cite the work as:

J. Mass, S. N. Srirama, C. Chang: *STEP-ONE: Simulated Testbed for Edge-Fog Processes based on the Opportunistic Network Environment Simulator*, Journal of Systems and Software, ISSN: 0164-1212, 2020. Elsevier. (Accepted for publication) 



