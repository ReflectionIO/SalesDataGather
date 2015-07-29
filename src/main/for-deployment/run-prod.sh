#!/bin/sh
java -jar ${project.build.finalName}.${project.packaging} --spring.profiles.active=prod