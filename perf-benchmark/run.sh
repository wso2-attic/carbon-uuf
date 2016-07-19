#!/usr/bin/env bash

baseDir=$(dirname "$0")
testsLoc="$baseDir/Tests"

function cleanup(){
    local tests=$(ls "$testsLoc")
    local type=""
    local test=""
    for test in $tests
    do
        local testDir="$testsLoc/$test"
        if [ -f "$testDir/run.sh" ]
        then
            local pidFile=$testDir/pid
            if [ -f $pidFile ]
            then
                $testDir/run.sh "stop"
            fi
        fi
    done
}

function buildSamples(){
    local tests=$(ls "$testsLoc")
    local type=""
    local test=""
    for test in $tests
    do
        local testDir="$testsLoc/$test"
        if [ -f "$testDir/run.sh" ]
        then
            $testDir/run.sh "build"
        fi
    done
}

if [ "$1" = "build" ]
then
    cleanup
    buildSamples
else
    cleanup
    $baseDir/excecute-tests.sh
fi

if [ ! "$?" = 0 ]
then
    echo "Test were not completed successfully"
    echo "Cleaning up.."
    cleanup
fi
