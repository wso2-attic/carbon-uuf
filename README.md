# Unified UI Framework

[![Build Status](https://wso2.org/jenkins/buildStatus/icon?job=carbon-uuf)](https://wso2.org/jenkins/view/All%20Builds/job/carbon-uuf/)

## Building From Source

You need to build following dependencies before building 'Carbon UUF'.

- Build 'Carbon UUF Maven Plugin' (for [samples](samples/))
  1. Clone [carbon-uuf-maven-plugin](https://github.com/wso2/carbon-uuf-maven-tools) repository.<br/>`git clone https://github.com/wso2/carbon-uuf-maven-tools.git`
  2. Build and install using Maven.<br/> `mvn clean install`

Then clone this repository (`git clone https://github.com/wso2/carbon-uuf.git`) and use Maven to build (`mvn clean install`). Built artifact can be found in `product/target/wso2uuf-1.0.0-SNAPSHOT.zip` path.

## How to run ?

Make sure you are running **JDK8 update 40** or latest.

1. Extract `wso2uuf-1.0.0-SNAPSHOT.zip` archive.
2. Go to `wso2uuf-1.0.0-SNAPSHOT/bin` direcotry.
4. Run `./carbon.sh` to start the Carbon server.
5. Visit sample apps.
  * Pets Store app [https://localhost:9292/pets-store/pets](https://localhost:9292/pets-store/pets)
  * Features App [https://localhost:9292/Features-app/](https://localhost:9292/Features-app/)

## Contributing to Carbon Maven UUF Plugin Project

Pull requests are highly encouraged and we recommend you to create a GitHub issue to discuss the issue or feature that you are contributing to.  

## License

Carbon UUF is available under the Apache 2.0 License.

## Copyright

Copyright (c) 2016, WSO2 Inc. (http://www.wso2.org) All Rights Reserved.
