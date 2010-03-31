@echo off

if "%JAVA_HOME%" == "" goto error

echo.
echo XT Builder
echo ----------------------

set ANT_HOME=.\ant

set LOCALCLASSPATH=%JAVA_HOME%\lib\tools.jar;%ANT_HOME%\lib\ant.jar;%ANT_HOME%\lib\xercesImpl.jar;%ANT_HOME%\lib\xalan.jar;%ANT_HOME%\lib\xml-apis.jar;%ANT_HOME%

"%JAVA_HOME%\bin\java.exe" -Djava_home="%JAVA_HOME%" -Dant.home="%ANT_HOME%" -classpath "%LOCALCLASSPATH%" org.apache.tools.ant.Main %1 %2 %3 %4 %5

goto end

:error

echo "ERROR: JAVA_HOME not found in your environment."
echo.
echo "Please set the JAVA_HOME variable in your environment to match the"
echo "location of the Java Virtual Machine you wish to use."

:end

set LOCALCLASSPATH=
set ANT_HOME=

rem # $Id$
