#!/bin/bash

cd ..
deploy_dir=`pwd`
isDebug=$1
JAR_NAME=obs-upload-0.0.1-SNAPSHOT.jar
APP_NAME=${deploy_dir}/$JAR_NAME
APP_CONFIG_NAME=${deploy_dir}/conf/application-dev.properties
APP_CONFIG_LOG=${deploy_dir}/conf/logback.xml


obsUploadPid=`ps -ef | grep -E $JAR_NAME | grep -v grep | awk '{print $2}'`
if [ ! -z "${obsUploadPid}" ]; then
    echo "obs-upload has started,do not start repeat... "
    exit 1
fi

if [ "$isDebug" == "-debug" ] || [ "$isDebug" == "-d" ]; then
    echo "debug is not supported"
else
    #>temp.log &
    mkdir -p BOOT-INF/classes/
    cp -rf $APP_CONFIG_NAME BOOT-INF/classes/application-dev.properties
    cp -rf $APP_CONFIG_LOG BOOT-INF/classes/logback.xml
    jar uvf $APP_NAME BOOT-INF/classes/application-dev.properties
    jar uvf $APP_NAME BOOT-INF/classes/logback.xml
    rm -rf BOOT-INF/
    result=`nohup java -jar -Dspring.config.location=$APP_CONFIG_NAME $APP_NAME >/dev/null 2>&1 &`
fi

echo "obs-upload start success"
exit 0