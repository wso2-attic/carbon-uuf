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
package org.wso2.carbon.uuf.handlebars;

import org.testng.Assert;
import org.testng.annotations.DataProvider;
import org.testng.annotations.Test;
import org.wso2.carbon.uuf.api.Placeholder;
import org.wso2.carbon.uuf.core.API;
import org.wso2.carbon.uuf.core.RequestLookup;
import org.wso2.carbon.uuf.renderablecreator.hbs.impl.js.JsFunctionsImpl;
import org.wso2.carbon.uuf.spi.HttpRequest;

import java.io.IOException;
import java.net.URISyntaxException;
import java.nio.file.Files;
import java.nio.file.Paths;

import static org.mockito.Mockito.mock;
import static org.mockito.Mockito.when;

/**
 * Test class for testing javascript functions used by the script engine in UUF.
 *
 * @since 1.0.0
 */
public class JsFunctionsTest {

    @DataProvider
    public Object[][] sendToClientJS() throws URISyntaxException, IOException {
        return new Object[][]{
                {"input1", String.join("\n", Files.
                        readAllLines(Paths.get(this.getClass().getResource("/input1.js").toURI()))),
                        String.join("\n", Files.
                                readAllLines(Paths.get(this.getClass().getResource("/output1.html").toURI())))},
                {"input2", String.join("\n", Files.
                        readAllLines(Paths.get(this.getClass().getResource("/input2.js").toURI()))),
                        String.join("\n", Files.
                                readAllLines(Paths.get(this.getClass().getResource("/output2.html").toURI())))},
        };
    }

    @Test(dataProvider = "sendToClientJS")
    public void testSendToClient(String varName, String inputJS, String outputJS) {
        API api = createAPI();
        JsFunctionsImpl jsFunctions = new JsFunctionsImpl(api);
        jsFunctions.getSendToClientFunction().call(varName, inputJS);
        Assert.assertEquals(api.getRequestLookup().getPlaceholderContent(Placeholder.js).get(), outputJS);
    }

    private static API createAPI() {
        API api = mock(API.class);
        when(api.getRequestLookup()).thenReturn(new RequestLookup("/contextPath", mock(HttpRequest.class), null));
        return api;
    }
}
