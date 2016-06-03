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

package org.wso2.carbon.uuf.internal.core.create;

import com.google.common.collect.HashMultimap;
import com.google.common.collect.SetMultimap;
import org.apache.commons.lang3.tuple.ImmutablePair;
import org.apache.commons.lang3.tuple.Pair;
import org.wso2.carbon.uuf.exception.MalformedConfigurationException;

import java.util.ArrayList;
import java.util.Deque;
import java.util.HashSet;
import java.util.LinkedList;
import java.util.List;
import java.util.Set;

public class DependencyTreeParser {

    public static Result parse(List<String> dependencyTreeLines) {
        // Flattened dependency map. key = component name, value = all dependencies of the 'key'
        SetMultimap<String, String> flattenedDependencies = HashMultimap.create();
        // Leveled dependencies. index = dependency level. index 0 == root component's dependencies
        List<Set<Pair<String, String>>> leveledDependencies = new ArrayList<>(6);

        int previousLevel = 0;
        String previousComponentName = null;
        Deque<Pair<String, List<String>>> parentNodesStack = new LinkedList<>();

        for (int i = 0; i < dependencyTreeLines.size(); i++) {
            String line = dependencyTreeLines.get(i);
            int level = countLevel(line);
            int jump = (level - previousLevel);
            Pair<String, String> componentNameVersion = getComponentNameVersion(line);

            if (level < leveledDependencies.size()) {
                leveledDependencies.get(level).add(componentNameVersion);
            } else {
                Set<Pair<String, String>> set = new HashSet<>();
                set.add(componentNameVersion);
                leveledDependencies.add(level, set);
            }

            if (i == 0) {
                previousComponentName = componentNameVersion.getLeft();
                continue;
            }
            if (jump < 0) {
                // Level decreased, so remove entries from the stack.
                for (int j = Math.abs(jump); j > 0; j--) {
                    Pair<String, List<String>> entry = parentNodesStack.removeLast();
                    flattenedDependencies.putAll(entry.getKey(), entry.getValue());
                }
            } else if (jump > 0) { // jump == 1
                // Level increased, so add an entry to the stack.
                parentNodesStack.add(new ImmutablePair<>(previousComponentName, new ArrayList<>(3)));
            }
            // (jump == 0): same level, nothing to do.

            // Add current componentName to all parent nodes as a dependency.
            for (Pair<String, List<String>> entry : parentNodesStack) {
                entry.getValue().add(componentNameVersion.getLeft());
            }

            previousLevel = level;
            previousComponentName = componentNameVersion.getLeft();
        }
        // If there is any remaining stack elements, add them to the flattenedDependencies.
        for (Pair<String, List<String>> entry : parentNodesStack) {
            flattenedDependencies.putAll(entry.getKey(), entry.getValue());
        }

        return new Result(flattenedDependencies, leveledDependencies);
    }

    private static int countLevel(String line) {
        int indent = 0;
        for (int i = 0; i < line.length(); i++) {
            char c = line.charAt(i);
            if (c == '+' || c == ' ' || c == '\\' || c == '|') {
                indent++;
            } else {
                break;
            }
        }
        return (indent <= 1) ? indent : (indent / 2);
    }

    private static Pair<String, String> getComponentNameVersion(String dependencyLine) {
        /* dependencyLine string should be in one of following formats.
         *  <group ID>:<artifact ID>:<artifact type>:<artifact version>
         *  <group ID>:<artifact ID>:<artifact type>:<artifact version>:compile
         *  (<group ID>:<artifact ID>:<artifact type>:<artifact version>:compile - omitted for duplicate)
         */
        String[] parts = dependencyLine.split(":");
        if ((parts.length != 4) && (parts.length != 5)) {
            throw new MalformedConfigurationException(
                    "Format of the dependency line '" + dependencyLine + "' is incorrect.");
        }
        // component name = <artifact ID> (2nd part), component version = <artifact version> (4th part)
        return Pair.of(parts[1], parts[3]);
    }

    public static class Result {

        private final SetMultimap<String, String> flattenedDependencies;
        private final List<Set<Pair<String, String>>> leveledDependencies;

        public Result(SetMultimap<String, String> flattenedDependencies,
                      List<Set<Pair<String, String>>> leveledDependencies) {
            this.flattenedDependencies = flattenedDependencies;
            this.leveledDependencies = leveledDependencies;
        }

        public SetMultimap<String, String> getFlattenedDependencies() {
            return flattenedDependencies;
        }

        public List<Set<Pair<String, String>>> getLeveledDependencies() {
            return leveledDependencies;
        }
    }
}
