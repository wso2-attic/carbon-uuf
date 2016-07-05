# Unified UI Framework

## Building From Source

You need to build following dependencies before building 'Carbon UUF'.

- Build 'Carbon UUF Maven Plugin' (for [samples](samples/sample4))
  1. Clone [carbon-uuf-maven-plugin](https://github.com/wso2/carbon-uuf-maven-plugin) repository.<br/>`git clone https://github.com/wso2/carbon-uuf-maven-plugin.git`
  2. Checkout *v1.0.0-m4* tag.<br/>`git checkout v1.0.0-m4`
  3. Build and install using Maven.<br/> `mvn clean install`

Then clone this repository (`git clone https://github.com/wso2/carbon-uuf.git`) and use Maven to build (`mvn clean install`). Built artifact can be found in `product/target/wso2uuf-1.0.0-SNAPSHOT.zip` path.

## How to run ?

1. Extract `wso2uuf-1.0.0-SNAPSHOT.zip` archive.
2. Go to `wso2uuf-1.0.0-SNAPSHOT/bin` direcotry.
4. Run `./carbon.sh` to start the Carbon server.
5. Visit sample apps.
  * Pets Store app [http://localhost:9090/pets-store/](http://localhost:9090/pets-store/)
  * Features App [https://localhost:9292/features-app/](https://localhost:9292/features-app/)

## Contributing to Carbon Maven UUF Plugin Project

Pull requests are highly encouraged and we recommend you to create a GitHub issue to discuss the issue or feature that you are contributing to.  

## License

Carbon UUF is available under the Apache 2.0 License.

## Copyright

Copyright (c) 2016, WSO2 Inc. (http://www.wso2.org) All Rights Reserved.
