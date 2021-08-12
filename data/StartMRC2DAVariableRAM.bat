@echo off
rem set HEAP_SIZE=8
set JAVA_COMMAND="jdk-13\bin\java.exe"

rem It is not necessary to modify the following section
set LOGGING_CONFIG_FILE=conf/logging.properties
set JAVA_PARAMETERS=-XX:+UseParallelGC -Djava.util.logging.config.file=%LOGGING_CONFIG_FILE% -XX:InitialRAMPercentage=40 -XX:MaxRAMPercentage=70 -splash:data/icons/caSplash.png
set CLASS_PATH=MRC2ToolBox-1.0.7-jar-with-dependencies.jar
set MAIN_CLASS=edu.umich.med.mrc2.datoolbox.main.MRC2ToolBoxCore

rem -Xms%HEAP_SIZE%g -Xmx%HEAP_SIZE%g 

rem Show java version, in case a problem occurs
%JAVA_COMMAND% -version

rem This command starts the Java Virtual Machine
%JAVA_COMMAND% %JAVA_PARAMETERS% -classpath %CLASS_PATH% %MAIN_CLASS%  

rem If there was an error, give the user chance to see it
IF ERRORLEVEL 1 pause

