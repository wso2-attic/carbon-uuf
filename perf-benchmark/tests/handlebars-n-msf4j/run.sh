#!/usr/bin/env bash

baseDir=$(dirname "$0")

artifactName="wso2uuf-*"
artifact="$baseDir/target/$artifactName"
startCmd="$artifact/bin/carbon.sh"

endpoints=""
endpoints+="Echo->http://localhost:9090/perf-bnchmrk/test/echo"

lookFor="WSO2 Carbon started"

waitTimeout=15

serverLog="$baseDir/target/server.log"
pidFile="$baseDir/target/pid"

function waitFor(){
    local waitTime=0
    while true
    do
        if [ $(tail "$serverLog" | grep "$lookFor" | wc -l) -gt 0 ]
        then
            echo true
            return
        fi
        sleep 1
        ((waitTime++))
        if [ $waitTime -gt $waitTimeout ]
        then
            echo false
            return
        fi
    done
}

function start(){
    stop
    nohup $startCmd > "$serverLog" &
    local pid=$!
    echo $pid > "$pidFile"
    local ret=$(waitFor)
    if [ $ret = "false" ]
    then
        echo "Unable to start server within $waitTimeout"
        stop
    else
        echo "Server started @ $pid"
    fi
}

function stop(){
    if [ -f "$pidFile" ]
    then
        local pid=$(cat "$pidFile")
        kill -9 $pid
        echo "Killed server @ $pid"
        rm -f "$pidFile"
    fi
    rm -f "$serverLog"
}

function getEndpoints(){
    echo "$endpoints"
}

function buildSample(){
    local curDir=$(pwd)
    local benchMarkApp="org.wso2.carbon.uuf.sample.perf-bnchmrk"
    rm -rf "$baseDir/target"
    find "$curDir/../product/target/" -iname "$artifactName.zip" -exec unzip -q {} -d "$baseDir/target/" \;
    cd "$baseDir/$benchMarkApp/"
    mvn clean install
    cd "$curDir"
    local extractedPackName=$(ls -1 $baseDir/target/|head -n1)
    find "$baseDir/$benchMarkApp/target/" -iname "$benchMarkApp*.zip" -exec unzip -q {} -d "$baseDir/target/$extractedPackName/deployment/uufapps/" \;
    cd "$curDir"
}

if [ "$1" = "start" ]
then
    start
elif [ "$1" = "stop" ]
then
    stop
    echo "Waiting for server termination"
    sleep 1
elif [ "$1" = "endpoints" ]
then
    getEndpoints
elif [ "$1" = "build" ]
then
    buildSample
fi
