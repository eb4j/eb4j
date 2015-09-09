#!/bin/bash

BIN_DIR=$(cd $(dirname $0) && pwd)
BASE_DIR=$(dirname $BIN_DIR)
LIB_DIR=$BASE_DIR/lib
TTF_DIR=$BASE_DIR/fonts
LOG4J_CONF=$BIN_DIR/log4j.xml
FONTS_PROPERTIES=$BIN_DIR/wdic-fonts.properties
CHARS_PROPERTIES=$BIN_DIR/wdic-chars.properties

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

$JAVACMD -Xmx2048m -cp $CP \
    -Dlog4j.configuration=file://$LOG4J_CONF \
    -Dttf.dir=$TTF_DIR \
    -Dwdic-fonts.properties=$FONTS_PROPERTIES \
    -Dwdic-chars.properties=$CHARS_PROPERTIES \
    fuku.xml2eb.converter.wdic.Wdic2Xml "$@"
