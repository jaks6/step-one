# Step-ONE
This project uses Gradle, tested with Java 1.8

You can specify exactly which version of Java to use by addings a 
gradle.properties file with contents (provide your exact JDK path):
org.gradle.java.home=/usr/lib/jvm/java-1.8.0-openjdk-amd64/

Run sample with:

gradlew run --args samples/pingpong/settings.txt
