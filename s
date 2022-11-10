#!/bin/bash

javac -cp .:hamcrest-core-1.3.jar:junit-4.12.jar chord/*.java

# run test

# Chord TEST
for i in {1..1}
do
     java -cp .:hamcrest-core-1.3.jar:junit-4.12.jar org.junit.runner.JUnitCore chord.ChordTest
done