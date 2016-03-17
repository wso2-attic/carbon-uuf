How to run
==========

1. go to carbon-uuf and build the repository
2. go to `product/target` and unzip wso2uuf-1.0.0-SNAPSHOT.zip
3. go to wso2uuf-1.0.0-SNAPSHOT/bin
4. run ./carbon.sh -DuufApps=../../../uuf-core/src/test/resources/simple-app

Developer Guide
===============

Pure Package
------------

Pure package is a java package in which
  * No IO/DB is being done
  * Classes are immutable
  * Each function's output depends only on its input (and some immutable class local variables)

Advantages
  * Trivially unit testable/ mockable
  * Concurrently callable
  * Easy to reason about

In this project we have maintained `wso2.carbon.uuf.core` and `org.wso2.carbon.uuf.handlebars` packages as pure packages.


