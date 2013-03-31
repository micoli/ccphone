#!/bin/bash
if [[ $1 == "mvn" ]]
then
mvn clean package
fi
java -jar target/ccphone-0.1-SNAPSHOT-jar-with-dependencies.jar