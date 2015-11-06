#!/bin/sh

mvn clean
mvn -Dmaven.test.skip package
rsync -avzih --update target/deploy/ mamin@gus:/opt/SalesDataGather/

