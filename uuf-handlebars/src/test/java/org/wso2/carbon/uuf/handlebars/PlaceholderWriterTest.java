/*
 *  Copyright (c) 2016, WSO2 Inc. (http://www.wso2.org) All Rights Reserved.
 *
 *  Licensed under the Apache License, Version 2.0 (the "License");
 *  you may not use this file except in compliance with the License.
 *  You may obtain a copy of the License at
 *
 *  http://www.apache.org/licenses/LICENSE-2.0
 *
 *  Unless required by applicable law or agreed to in writing, software
 *  distributed under the License is distributed on an "AS IS" BASIS,
 *  WITHOUT WARRANTIES OR CONDITIONS OF ANY KIND, either express or implied.
 *  See the License for the specific language governing permissions and
 *  limitations under the License.
 */

package org.wso2.carbon.uuf.handlebars;

import org.testng.Assert;
import org.testng.annotations.Test;

import java.io.IOException;
import java.util.HashMap;
import java.util.Map;

public class PlaceholderWriterTest {

    @Test
    public void testWithoutPlaceholders() throws IOException {
        PlaceholderWriter placeholderWriter = new PlaceholderWriter();
        placeholderWriter.write("First line.\n");
        placeholderWriter.write("Second line.\n");
        placeholderWriter.write("Third line.\n");
        placeholderWriter.write("Final text.");
        Assert.assertEquals(placeholderWriter.toString(), "First line.\nSecond line.\nThird line.\nFinal text.");
    }

    @Test
    public void testWithPlaceholders() throws IOException {
        PlaceholderWriter placeholderWriter = new PlaceholderWriter();
        placeholderWriter.addPlaceholder("p0");
        placeholderWriter.write("First line.\n");
        placeholderWriter.write("Second line.\n");
        placeholderWriter.addPlaceholder("p1");
        placeholderWriter.addPlaceholder("p2");
        placeholderWriter.write("Third line.\n");
        placeholderWriter.write("Forth line.\n");
        placeholderWriter.addPlaceholder("p3");
        Map<String, String> placeholderValues = new HashMap<>(4);
        placeholderValues.put("p0", "Placeholder zero,");
        placeholderValues.put("p1", "Placeholder one,");
        placeholderValues.put("p2", "Placeholder two,");
        placeholderValues.put("p3", "Placeholder three");
        String output = placeholderWriter.toString(placeholderValues);
        output = output.replaceAll("<!--.*-->\n", "");
        String expectedOutput = "Placeholder zero,First line.\nSecond line.\nPlaceholder one,Placeholder two," +
                "Third line.\nForth line.\nPlaceholder three";
        Assert.assertEquals(output, expectedOutput);
    }
}