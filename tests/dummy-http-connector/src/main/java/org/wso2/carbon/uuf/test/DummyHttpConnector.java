/*
 *  Copyright (c) 2016, WSO2 Inc. (http://www.wso2.org) All Rights Reserved.
 *
 *  WSO2 Inc. licenses this file to you under the Apache License,
 *  Version 2.0 (the "License"); you may not use this file except
 *  in compliance with the License.
 *  You may obtain a copy of the License at
 *
 *  http://www.apache.org/licenses/LICENSE-2.0
 *
 *  Unless required by applicable law or agreed to in writing,
 *  software distributed under the License is distributed on an
 *  "AS IS" BASIS, WITHOUT WARRANTIES OR CONDITIONS OF ANY
 *  KIND, either express or implied.  See the License for the
 *  specific language governing permissions and limitations
 *  under the License.
 */

package org.wso2.carbon.uuf.test;

import org.osgi.service.component.annotations.Component;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import org.wso2.carbon.uuf.api.Server;
import org.wso2.carbon.uuf.spi.HttpConnector;

@Component(name = "org.wso2.carbon.uuf.test.DummyHttpConnector",
           service = HttpConnector.class,
           immediate = true)
@SuppressWarnings("unused")
public class DummyHttpConnector implements HttpConnector {

    private static final Logger LOGGER = LoggerFactory.getLogger(DummyHttpConnector.class);

    @Override
    public void setServer(Server server) {
        LOGGER.info("Server '{}' registered to '{}'.", server, getClass().getName());
    }

    public void registerAppContextPath(String appContextPath) {
        LOGGER.info("App context path '{}' registered to '{}'.", appContextPath, getClass().getName());
    }
}
