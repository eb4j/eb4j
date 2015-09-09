#!/bin/bash

BIN_DIR=`dirname $0`
LIB_DIR=$BIN_DIR/../lib

for jar in $LIB_DIR/*.jar; do
 if [ -z "$CP" ]; then
   CP=$jar
 else
   CP=$CP:$jar
 fi
done

JAVACMD=java
if [ -n "$JAVA_HOME" ]; then
  if [ -x "$JAVA_HOME/jre/bin/java" ]; then
    JAVACMD="$JAVA_HOME/jre/bin/java"
  else
    JAVACMD="$JAVA_HOME/bin/java"
  fi
fi

$JAVACMD -cp $CP fuku.eb4j.tool.EBZip "$@"
