#!/bin/bash

javac -cp .:hamcrest-core-1.3.jar:junit-4.12.jar chord/*.java

# Chord SIMU
java -cp .:hamcrest-core-1.3.jar:junit-4.12.jar org.junit.runner.JUnitCore chord.ChordTest
