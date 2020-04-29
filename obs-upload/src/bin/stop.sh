#! /bin/bash

cd ..
deploy_dir=`pwd`
JAR_NAME=obs-upload-0.0.1-SNAPSHOT.jar
APP_NAME=$deploy_dir/$JAR_NAME

obsUploadPid=`ps -ef | grep -E $JAR_NAME | grep -v grep | awk '{print $2}'`
if [ ! -z "${obsUploadPid}" ]; then
    for PID in "obsUploadPid"
    do
        if [ ! -z "$PID" ]; then
            kill -9 $PID
            echo "previous obs-upload has been killed... "
        fi
    done
fi

sleep 1
echo "obs-upload stop success"
exit 0