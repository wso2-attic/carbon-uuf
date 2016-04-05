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

package org.wso2.carbon.uuf.handlebars.helpers.runtime;

import com.github.jknack.handlebars.Helper;
import com.github.jknack.handlebars.Options;
import org.wso2.carbon.uuf.core.Fragment;

import java.io.IOException;
import java.util.Deque;

import static org.wso2.carbon.uuf.handlebars.HbsRenderable.COMPONENT_NAME_KEY;
import static org.wso2.carbon.uuf.handlebars.HbsRenderable.FRAGMENTS_STACK_KEY;
import static org.wso2.carbon.uuf.handlebars.HbsRenderable.URI_KEY;

public class PublicHelper implements Helper<String> {

    public static final String HELPER_NAME = "public";

    @Override
    public CharSequence apply(String relativeUri, Options options) throws IOException {
        if (!relativeUri.startsWith("/")) {
            throw new IllegalArgumentException("Public resource URI should start with '/'.");
        }
        Deque<Fragment> fragmentStack = options.data(FRAGMENTS_STACK_KEY);
        String uriUpToContext = options.data(URI_KEY);
        String component = options.data(COMPONENT_NAME_KEY);
        String publicUri;
        if ((fragmentStack == null) || fragmentStack.isEmpty()) {
            // this resource is adding in a page or layout
            publicUri = uriUpToContext + "/public" + component + "/base" + relativeUri;
        } else {
            publicUri = uriUpToContext + "/public" + fragmentStack.peekLast().getPublicContext()
                    + relativeUri;
        }
        return publicUri;
    }
}
