# STEP-ONE
STEP-ONE is packaged as a *Gradle* project.

Run a specific simulation settings file with:

    gradlew run --args samples/pingpong/settings.txt



## Step-ONE modeller:

To produce BPMN process models for STEP-ONE, use the STEP-ONE Flowable Modeller web application:

* Install Docker + Docker Compose

* Get the STEP-ONE Modeller:
  http://kodu.ut.ee/~jaks/public/step-one/step1-modeler-docker.zip 

* Unpack the archive, and in folder with *docker-compose.yml*, run: 
  * 	docker-compose up

* After the web-app service has finished initialising, you can open the modeller at:
  http://localhost:8888/flowable-modeler

  

## Documentation 

See the [examples and tutorials page](https://jaks6.github.io/step-one/)



## Publications

Please cite the work as:

J. Mass, S. N. Srirama, C. Chang: STEP-ONE: Simulated Testbed for Edge-Fog Processes based on the Opportunistic Network Environment Simulator, Journal of Systems and Software, ISSN: 0164-1212, 2020. Elsevier. (Accepted for publication) 





Tested with Java 1.8

You can specify exactly which version of Java to use by adding a *gradle.properties* file with contents (provide your exact JDK path):

    org.gradle.java.home=/usr/lib/jvm/java-1.8.0-openjdk-amd64/
