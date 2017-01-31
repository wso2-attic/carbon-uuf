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
import org.wso2.carbon.uuf.renderablecreator.hbs.core.HbsRenderable;

import java.io.IOException;

import static org.wso2.carbon.uuf.spi.HttpRequest.COOKIE_CSRFTOKEN;

public class CSRFTokenHelper implements Helper<Object> {

    public static final String HELPER_NAME = "csrfToken";

    @Override
    public CharSequence apply(Object name, Options options) throws IOException {
        StringBuilder buffer = new StringBuilder();
        RequestLookup requestLookup = options.data(HbsRenderable.DATA_KEY_REQUEST_LOOKUP);
        String cookieValue = requestLookup.getRequest().getCookieValue(COOKIE_CSRFTOKEN);

        if (cookieValue != null) {
            buffer.append("<input type=\"hidden\" name=\"csrftoken\" id=\"csrftoken\" value=\"")
                    .append(cookieValue)
                    .append("\"/>");
        }

        return new Handlebars.SafeString(buffer.toString());
    }
}
