#!/bin/sh

CLASSPATH=$CLASSPATH:/home/vion/workspace/cspom/target/classes:/home/vion/workspace/cspfj/target/classes:/home/vion/.m2/repository/org/kohsuke/bzip2/1.0/bzip2-1.0.jar

scala xcsp.XCSPSolver a=b file://$1
