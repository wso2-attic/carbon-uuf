##Echo page performance test

In this performance test, a sample echo page was created with each subjected templating and http-connector. Echo page is ~10kb without any compression.

## Prerequisite
* **apache2-utils** - This performance tests are executed using ApacheBench. Therefore in order to run the tests, apache2-utils
should be installed in the machine.

## Throughput test

To measure the throughput, each of the above mentioned sample services were started and 1KB of payload was sent to 
each service repeatedly in varying concurrency levels using apache bench tool. After that the average throughput for
each concurrency level is calculated and plotted for each framework.

All services were run out of the box without any tuning separately on a 32 core 64GB server in JVM v1.8.0_60 with default configuration.

### Performing the throughput test

Build the samples using the following command from [perf-benchmark](perf-benchmark)

```
./run.sh build
```

Run all tests using the following command from [perf-benchmark](perf-benchmark)

```
./run.sh
```

This script will perform the loads and provide you the average throughput, latency and all the percentiles for all the sample services.

You can customize the following parameters of the performance test by modifying the following values in [excecute-tests.sh](excecute-tests.sh)
 * concLevels - Space separated list of concurrency levels to test
 * perTestTime - Maximum time to spend on a single concurrency level
 * testLoops - Number of requests to perform for a sigle concurrency level
 * warmUpConc - Concurrency of the warm-up requests
 * warmUpLoop - Number of requests to send for warm-up