#!/bin/bash

JAR_FILE="sqlite-jdbc-3.49.1.0.jar"

if [ ! -f "$JAR_FILE" ]; then
    echo "$JAR_FILE not found! Please install sqlite-jdbc and try again."
    exit 1
else
    echo "$JAR_FILE found. Compiling and running the program..."

    javac -cp ".:$JAR_FILE" *.java

    if [ $? -eq 0 ]; then
        java -cp ".:$JAR_FILE" Main
    else
        echo "Compilation failed. Please check your Java code."
        exit 1
    fi
fi
