#!/bin/bash
javac -cp ".:sqlite-jdbc-3.49.1.0.jar" *.java
java -cp ".:sqlite-jdbc-3.49.1.0.jar" Main
