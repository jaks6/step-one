# Step-ONE
This project uses Gradle, tested with Java 1.8

You can specify exactly which version of Java to use by addings a 
gradle.properties file with contents (provide your exact JDK path):
org.gradle.java.home=/usr/lib/jvm/java-1.8.0-openjdk-amd64/

Run sample with:

    gradlew run --args samples/pingpong/settings.txt




## Step-ONE modeller:
Install Docker + Docker Compose

Get the Step ONE Flowable Modeller:
http://kodu.ut.ee/~jaks/public/step-one/step1-modeler-docker.zip 

Unpack the archive, and in folder with docker-compose.yml, run
    docker-compose up

After the web-app service has finished initializing, you can open the modeler at:
http://localhost:8888/flowable-modeler
