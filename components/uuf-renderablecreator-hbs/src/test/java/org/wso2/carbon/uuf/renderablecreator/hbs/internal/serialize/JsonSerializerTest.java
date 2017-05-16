/*
 * Copyright (c) 2017, WSO2 Inc. (http://www.wso2.org) All Rights Reserved.
 *
 * WSO2 Inc. licenses this file to you under the Apache License,
 * Version 2.0 (the "License"); you may not use this file except
 * in compliance with the License.
 * You may obtain a copy of the License at
 *
 * http://www.apache.org/licenses/LICENSE-2.0
 *
 * Unless required by applicable law or agreed to in writing,
 * software distributed under the License is distributed on an
 * "AS IS" BASIS, WITHOUT WARRANTIES OR CONDITIONS OF ANY
 * KIND, either express or implied.  See the License for the
 * specific language governing permissions and limitations
 * under the License.
 */

package org.wso2.carbon.uuf.renderablecreator.hbs.internal.serialize;

import jdk.nashorn.api.scripting.NashornScriptEngineFactory;
import org.apache.commons.io.IOUtils;
import org.testng.Assert;
import org.testng.annotations.DataProvider;
import org.testng.annotations.Test;

import java.io.IOException;
import java.util.List;
import javax.script.Bindings;
import javax.script.ScriptContext;
import javax.script.ScriptEngine;
import javax.script.ScriptException;

/**
 * Created by sajith on 5/9/17.
 */
public class JsonSerializerTest {

    private static final int NUMBER_OF_TESTS = 6;

    @DataProvider
    public Object[][] safeTestData() throws IOException {
        return getTestData("output");
    }

    @DataProvider
    public Object[][] prettyTestData() throws IOException {
        return getTestData("pretty-output");
    }

    @Test(dataProvider = "safeTestData")
    public void testToJson(String jsScript, String expectedJson) throws ScriptException {
        Object jsObject = executeJavaScript(jsScript).get("input");
        String actualJson = JsonSerializer.toSafeJson(jsObject);
        Assert.assertEquals(actualJson, expectedJson);
    }

    @Test(dataProvider = "prettyTestData")
    public void testToPrettyJson(String jsScript, String expectedJson) throws ScriptException {
        Object jsObject = executeJavaScript(jsScript).get("input");
        String actualJson = JsonSerializer.toPrettyJson(jsObject);
        Assert.assertEquals(actualJson, expectedJson);
    }

    private static Object[][] getTestData(String outputFileNamePrefix) throws IOException {
        Object[][] data = new Object[NUMBER_OF_TESTS][2];
        for (int i = 1; i <= NUMBER_OF_TESTS; i++) {
            data[i - 1] = new Object[]{readResource("/serialize/input" + i + ".js"),
                    readResource("/serialize/" + outputFileNamePrefix + i + ".txt")};
        }
        return data;
    }

    private static String readResource(String path) throws IOException {
        List<String> lines = IOUtils.readLines(JsonSerializerTest.class.getResourceAsStream(path),
                                               "UTF-8");
        return String.join("\n", lines);
    }

    private static Bindings executeJavaScript(String jsScript) throws ScriptException {
        NashornScriptEngineFactory scriptEngineFactory = new NashornScriptEngineFactory();
        ScriptEngine engine = scriptEngineFactory.getScriptEngine("-strict", "--optimistic-types");
        engine.eval(jsScript);
        return engine.getBindings(ScriptContext.ENGINE_SCOPE);
    }
}
