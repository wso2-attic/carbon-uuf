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

package org.wso2.carbon.uuf;

import com.google.common.collect.SetMultimap;
import org.apache.commons.lang3.tuple.Pair;
import org.testng.Assert;
import org.testng.annotations.Test;
import org.wso2.carbon.uuf.internal.core.create.DependencyTreeParser;

import java.util.ArrayList;
import java.util.List;
import java.util.Scanner;
import java.util.Set;

public class DependencyTreeParserTest {

    public static List<String> getDependencyTreeLines() {
        List<String> dependencyTreeLines = new ArrayList<>();
        Scanner scanner = new Scanner(DependencyTreeParserTest.class.getResourceAsStream("/test-dependency.tree"));
        while (scanner.hasNextLine()) {
            dependencyTreeLines.add(scanner.nextLine());
        }
        return dependencyTreeLines;
    }

    @Test
    public void testParse() {
        DependencyTreeParser.Result result = DependencyTreeParser.parse(getDependencyTreeLines());

        SetMultimap<String, String> flattenedDependencies = result.getFlattenedDependencies();
        Assert.assertEquals(flattenedDependencies.get("org.wso2.uuf.core").size(), 41);
        Assert.assertEquals(flattenedDependencies.get("snakeyaml").size(), 0);
        Assert.assertEquals(flattenedDependencies.get("handlebars").size(), 0);
        Assert.assertEquals(flattenedDependencies.get("cache-api").size(), 0);
        Assert.assertEquals(flattenedDependencies.get("org.wso2.carbon.caching").size(), 3);
        Assert.assertEquals(flattenedDependencies.get("msf4j-core").size(), 12);
        Assert.assertEquals(flattenedDependencies.get("org.wso2.carbon.transport.http.netty").size(), 1);
        Assert.assertEquals(flattenedDependencies.get("org.apache.servicemix.bundles.commons-beanutils").size(), 1);
        Assert.assertEquals(flattenedDependencies.get("nimbus-jose-jwt").size(), 3);
        Assert.assertEquals(flattenedDependencies.get("slf4j-log4j12").size(), 1);
        Assert.assertEquals(flattenedDependencies.get("testng").size(), 2);
        Assert.assertEquals(flattenedDependencies.get("mockito-core").size(), 2);

        List<Set<Pair<String, String>>> leveledDependencies = result.getLeveledDependencies();
        Assert.assertEquals(leveledDependencies.get(0).size(), 1);
        Assert.assertTrue(leveledDependencies.get(0).contains(Pair.of("org.wso2.uuf.core", "1.0.0-SNAPSHOT")));
        Assert.assertEquals(leveledDependencies.get(1).size(), 21);
        Assert.assertEquals(leveledDependencies.get(2).size(), 15);
        Assert.assertEquals(leveledDependencies.get(3).size(), 5);
        Assert.assertTrue(leveledDependencies.get(3).contains(Pair.of("org.wso2.carbon.core", "5.0.0")));
        Assert.assertTrue(leveledDependencies.get(3).contains(Pair.of("commons-logging", "1.1.1")));
        Assert.assertTrue(leveledDependencies.get(3).contains(Pair.of("jcip-annotations", "1.0")));
        Assert.assertTrue(leveledDependencies.get(3).contains(Pair.of("json-smart", "1.1.1")));
        Assert.assertTrue(leveledDependencies.get(3).contains(Pair.of("bcprov-jdk15on", "1.50")));
    }
}