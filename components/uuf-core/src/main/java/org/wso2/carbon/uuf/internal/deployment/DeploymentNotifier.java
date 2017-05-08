/*
 * Copyright (c) 2016, WSO2 Inc. (http://www.wso2.org) All Rights Reserved.
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

package org.wso2.carbon.uuf.internal.deployment;

import org.apache.commons.lang3.tuple.Pair;

import java.util.List;

/**
 * A notifier that notify app deployment event to others.
 *
 * @since 1.0.0
 */
public interface DeploymentNotifier {

    /**
     * Notifies relevant components/services about the availability of the specified apps.
     *
     * @param appNamesContextPaths names and context paths of the available apps
     */
    void notify(List<Pair<String, String>> appNamesContextPaths);
}
