#!/usr/bin/env bash

baseDir=$(dirname "$0")
concLevels="1 25 50 100 200 400 800 1600 3200"
perTestTime=30
testLoops=1000000
warmUpConc=200
warmUpLoop=50000

tmpDir="$baseDir/tmp"
testsLoc="$baseDir/Tests"
payload="$baseDir/target/1kb_rand_data.txt"
timeStmp=$(date +%s)

types=()
declare -A MAP

function printResultStructures(){
    echo "Printing types.."
    for type in ${types[@]}
    do
        echo "$type"
    done
    echo ""
    echo "Printing results map.."
    for key in ${!MAP[@]}
    do
      echo "$key -> ${MAP[$key]}"
    done
}

function hash(){
    echo -n $1 | md5sum | awk '{print $1}'
}

function addNewType(){
    local newType=$1
    local isNew=true
    local type=""
    for type in ${types[@]}
    do
        if [ "$type" = "$newType" ]
        then
            isNew=false
            break
        fi
    done
    if "$isNew"
    then
        types+=($newType)
    fi
}

function addNewTest(){
    local type=$1
    local test=$2
    if [ ${MAP["$type-test-n"]} ]
    then
        local testN=${MAP["$type-test-n"]}
        MAP["$type-test-$testN"]="$test"
        MAP["$type-test-n"]=$((testN+1))
    else
        MAP["$type-test-0"]="$test"
        MAP["$type-test-n"]=1
    fi
}

function processResults(){
    local metric=$1
    local resultsFile=$2
    local type=""
    local testI=0
    local conc=""

    rm -f "$resultsFile"

    for type in "${types[@]}"
    do
        echo "Test: $type," >> "$resultsFile"
        local isPrintH=true
        for conc in $concLevels
        do
            local header=""
            if "$isPrintH"
            then
                header="Concurrency"
            fi
            local line="$conc"
            for testI in $(seq 0 $((MAP["$type-test-n"]-1)))
            do
                local test=${MAP["$type-test-$testI"]}
                if "$isPrintH"
                then
                    header+=", $test"
                fi
                local tps=${MAP["$type-$test-$conc-$metric"]}
                line+=", $tps"
            done
            if "$isPrintH"
            then
                echo "$header" >> "$resultsFile"
                isPrintH=false
            fi
            echo "$line" >> "$resultsFile"
        done
        echo "" >> "$resultsFile"
    done
    echo "==========================================="
    echo "            Results ($metric)              "
    echo "==========================================="
    cat "$resultsFile"
}

function processPercentiles(){
    local resultsFile=$1
    local type=""
    local testI=0
    local conc=""

    rm -f "$resultsFile"

    local header="Test, Concurrency"
    for hVal in $(seq 0 100)
    do
        header+=", $hVal"
    done

    for type in "${types[@]}"
    do
        echo "Test: $type," >> "$resultsFile"
        for conc in $concLevels
        do
            local isPrintH=true
            for testI in $(seq 0 $((MAP["$type-test-n"]-1)))
            do
                local test=${MAP["$type-test-$testI"]}
                local percents=${MAP["$type-$test-$conc-percents"]}
                if "$isPrintH"
                then
                    echo "$header" >> "$resultsFile"
                    isPrintH=false
                fi
                echo "$percents" >> "$resultsFile"
            done
            echo "" >> "$resultsFile"
        done
    done
    echo "==========================================="
    echo "            Results (Percentiles)              "
    echo "==========================================="
    cat "$resultsFile"
}

function warmUp(){
    local service=$1
    echo "Warm up service $service"
    ab -k -p "$payload" -c $warmUpConc -n $warmUpLoop "$service" > /dev/null
}

function testConcLevel(){
    local service=$1
    local concLevel=$2
    local type=$3
    local test=$4

    local resOut="$tmpDir/result-$type-$test-conc$concLevel-rep$loopRep-loops$testLoops-time$timeStmp-$(uuidgen)"
    local percentOut="$tmpDir/percentile-$type-$test-conc$concLevel-rep$loopRep-loops$testLoops-time$timeStmp-$(uuidgen)"
    echo "Testing service: $service"
    echo "Testing concurrency $concLevel at $resOut"
    ab -t "$perTestTime" -n "$testLoops" -c "$concLevel" -k -e "$percentOut" "$service" > "$resOut"

    local tps=$(cat "$resOut" | grep -Eo "Requests per second.*" | grep -Eo "[0-9]+" | head -1)

    local meanLat=$(cat "$resOut" | grep -Eo "Time per request.*\(mean\)" | grep -Eo "[0-9]+(\.[0-9]+)?")

    local percents=$(cat "$percentOut" | grep -Eo ",.*" | grep -Eo "[0-9]+(\.[0-9]+)?" | tr '\n' ',')
    percents="$test, $concLevel, $percents"

    echo "For $service at concurrency $concLevel"

    MAP["$type-$test-$concLevel-tps"]=$tps
    echo -e "\tThroughput $tps"

    MAP["$type-$test-$concLevel-meanLat"]=$meanLat
    echo -e "\tMean latency is $meanLat"

    MAP["$type-$test-$concLevel-percents"]=$percents
    echo -e "\tPercentiles are $percents"
}

function iterateConcLevels(){
    local testDir=$1
    local type=$2
    local test=$3
    local service=$4
    echo "Testing concurrency levels in $testDir"
    warmUp "$service" # Warm up the service before getting results
    local concLevel=""
    for concLevel in $concLevels
    do
        testConcLevel "$service" "$concLevel" "$type" "$test"
    done
}

function iterateTests(){
    local tests=$(ls "$1")
    local type=""
    local test=""
    for test in $tests
    do
        local testDir="$1/$test"
        if [ -f "$testDir/run.sh" ]
        then
            local startState=$($testDir/run.sh "start")
            if [[ "$startState" = *"Server started"* ]]
            then
                echo "Server started in $testDir"
                local endpoints=$($testDir/run.sh "endpoints")
                endpoints=${endpoints//","/" "}
                for endpoint in $endpoints
                do
                    endpoint=(${endpoint//"->"/" "})
                    local type=${endpoint[0]}
                    local service=${endpoint[1]}
                    addNewType "$type"
                    addNewTest "$type" "$test"
                    iterateConcLevels "$testDir" "$type" "$test" "$service"
                done
                $testDir/run.sh "stop"
            else
                echo "Server not started in $testDir"
            fi
        fi
    done

    processResults "tps" "$baseDir/target/results-tps.csv"
    echo ""
    processResults "meanLat" "$baseDir/target/results-latency.csv"
    echo ""
    processPercentiles "$baseDir/target/results-percentiles.csv"
    echo ""
    #printResultStructures
}

mkdir -p "$tmpDir"
base64 /dev/urandom | head -c 1024 > "$payload"

iterateTests "$testsLoc"
