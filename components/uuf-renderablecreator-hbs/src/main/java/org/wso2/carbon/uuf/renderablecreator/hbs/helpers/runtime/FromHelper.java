/*
 *  Copyright (c) 2017, WSO2 Inc. (http://www.wso2.org) All Rights Reserved.
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

package org.wso2.carbon.uuf.renderablecreator.hbs.helpers.runtime;

import com.github.jknack.handlebars.Handlebars;
import com.github.jknack.handlebars.Helper;
import com.github.jknack.handlebars.Options;
import org.wso2.carbon.uuf.core.RequestLookup;
import org.wso2.carbon.uuf.internal.auth.SessionRegistry;
import org.wso2.carbon.uuf.renderablecreator.hbs.core.HbsRenderable;

import java.io.IOException;

public class FromHelper implements Helper<String> {

    public static final String HELPER_NAME = "form";

    @Override
    public CharSequence apply(String name, Options options) throws IOException {
        StringBuilder buffer = new StringBuilder();

        RequestLookup requestLookup = options.data(HbsRenderable.DATA_KEY_REQUEST_LOOKUP);
        String cookieValue = requestLookup.getRequest().getCookieValue(SessionRegistry.CSRF_TOKEN);

        buffer.append("<form name=\"" + name + "\"");
        // set all the form attributes, such as "action", "method" etc
        options.hash.entrySet().forEach(entry -> buffer.append(" " + entry.getKey() + "=\"" + entry.getValue() + "\""));
        buffer.append(">").append(options.fn().toString());
        // append the CSRFTOKEN cookie value as a hidden filed
        if (cookieValue != null) {
            buffer.append("\t<input type=\"hidden\" name=\"csrftoken\" id=\"csrftoken\" value=\"" + cookieValue + "\"/>\n");
        }
        buffer.append("</form>");
        return new Handlebars.SafeString(buffer.toString());
    }
}