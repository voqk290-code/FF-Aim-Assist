#!/bin/sh
JAVA_HOME=${JAVA_HOME:-/usr}
CLASSPATH=./gradle/wrapper/gradle-wrapper.jar
exec $JAVA_HOME/bin/java -cp $CLASSPATH org.gradle.wrapper.GradleWrapperMain "$@"
