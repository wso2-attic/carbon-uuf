# Unified UI Framework

[![Build Status](https://wso2.org/jenkins/buildStatus/icon?job=carbon-uuf)](https://wso2.org/jenkins/view/All%20Builds/job/carbon-uuf/)

## Building From Source

1. Clone this repository (`git clone https://github.com/wso2/carbon-uuf.git`) 
2. Use maven to build (`mvn clean install`). 
3. Built distribution archive can be found in `product/target/wso2uuf-1.0.0-SNAPSHOT.zip` path.

## How to run ?

Make sure you are running **JDK8 update 40** or latest.

1. Extract `wso2uuf-1.0.0-SNAPSHOT.zip` archive.
2. Go to `wso2uuf-1.0.0-SNAPSHOT/bin` direcotry.
3. Run `./carbon.sh` to start the Carbon server.
4. Visit sample apps as below. They demonstrate the 
  * Features App [https://localhost:9292/Features-app/](https://localhost:9292/Features-app/)
    - This app showcases all the functionalities supported by UUF, including the all the in-built handlebars helpers.
  * Pets Store app [https://localhost:9292/pets-store/pets](https://localhost:9292/pets-store/pets)
    - This app demonstrates a simple pet store scenario.
5. UUF artifacts are created using the [carbon-uuf-maven-plugin](https://github.com/wso2/carbon-uuf-maven-tools). You can also refer the plugin [documentaion](https://github.com/wso2/carbon-uuf-maven-tools/tree/master/plugin) on how to create apps, components, themes using the plugin.

## Contributing to Carbon UUF and Carbon UUF Maven Plugin

Pull requests are highly encouraged and we recommend you to create a GitHub issue to discuss the issue or feature that you are contributing to.

## License

Carbon UUF is available under the Apache 2.0 License.

## Copyright

Copyright (c) 2016, WSO2 Inc. (http://www.wso2.org) All Rights Reserved.
