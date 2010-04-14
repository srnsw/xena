#!/bin/sh
# $Id$

echo
echo "XT Builder"
echo "----------------------"

if [ "$JAVA_HOME" = "" ] ; then
  echo "ERROR: JAVA_HOME not found in your environment."
  echo
  echo "Please set the JAVA_HOME variable in your environment to match the"
  echo "location of the Java Virtual Machine you want to use."
  return
fi

#
# Set PATH separator character based on test to see if running 
# under Cygwin, or real UNIX...
#

echo `uname` | grep -q "CYGWIN" && S=";" || S=":"

ANT_HOME=./ant
LOCALCLASSPATH=${JAVA_HOME}/lib/tools.jar${S}${ANT_HOME}/lib/ant-launcher.jar${S}${ANT_HOME}/lib/ant.jar${S}${ANT_HOME}/lib/ant-nodeps.jar${S}${ANT_HOME}/lib/dost1.0.jar${S}${ANT_HOME}/lib/xercesImpl.jar${S}${ANT_HOME}/lib/ant-trax.jar${S}${ANT_HOME}

echo
echo Building with classpath $LOCALCLASSPATH

echo
echo Starting Ant...

"$JAVA_HOME/bin/java" -Djava_home="$JAVA_HOME" -Dant.home=$ANT_HOME -classpath $LOCALCLASSPATH org.apache.tools.ant.launch.Launcher $*
