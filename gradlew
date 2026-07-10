#!/bin/sh
#
# Gradle start up script for POSIX compatible shells (sh, dash, ksh, zsh, bash)
##############################################################################
DEFAULT_JVM_OPTS='"-Xmx64m" "-Xms64m"'
APP_NAME="Gradle"
APP_BASE_NAME=$(basename "$0")
APP_HOME=$(cd "$(dirname "$0")" && pwd -P)
MAX_FD="maximum"

warn () { echo "$*"; }
die () { echo "$*" >&2; exit 1; }

if [ "$APP_HOME" = "" ]; then APP_HOME="."; fi

CLASSPATH=$APP_HOME/gradle/wrapper/gradle-wrapper.jar

# Determine the Java command to use to start the JVM.
if [ -n "$JAVA_HOME" ] ; then
    JAVACMD="$JAVA_HOME/bin/java"
else
    JAVACMD="java"
fi

exec "$JAVACMD" \
     $DEFAULT_JVM_OPTS \
     $JAVA_OPTS \
     $GRADLE_OPTS \
     "-Dorg.gradle.appname=$APP_BASE_NAME" \
     -classpath "$CLASSPATH" \
     org.gradle.wrapper.GradleWrapperMain \
     "$@"
