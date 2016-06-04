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

package org.wso2.carbon.uuf.internal.debug;

import com.google.gson.JsonArray;
import com.google.gson.JsonObject;
import org.apache.commons.io.IOUtils;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import org.wso2.carbon.uuf.api.HttpRequest;
import org.wso2.carbon.uuf.api.HttpResponse;
import org.wso2.carbon.uuf.core.App;
import org.wso2.carbon.uuf.core.Component;
import org.wso2.carbon.uuf.internal.core.UriPatten;

import java.io.IOException;
import java.io.InputStream;
import java.lang.management.ManagementFactory;
import java.util.Map;

import static org.wso2.carbon.uuf.api.HttpResponse.CONTENT_TYPE_APPLICATION_JSON;
import static org.wso2.carbon.uuf.api.HttpResponse.CONTENT_TYPE_TEXT_HTML;
import static org.wso2.carbon.uuf.api.HttpResponse.STATUS_BAD_REQUEST;
import static org.wso2.carbon.uuf.api.HttpResponse.STATUS_INTERNAL_SERVER_ERROR;
import static org.wso2.carbon.uuf.api.HttpResponse.STATUS_OK;
import static org.wso2.carbon.uuf.api.HttpResponse.STATUS_NOT_FOUND;

public class Debugger {

    public static final UriPatten URI_PATTEN_API_PAGES = new UriPatten("/debug/api/pages/");
    public static final UriPatten URI_PATTEN_API_LAYOUTS = new UriPatten("/debug/api/layouts/");
    public static final UriPatten URI_PATTEN_API_FRAGMENTS = new UriPatten("/debug/api/fragments/");
    public static final UriPatten URI_PATTEN_API_THEMES = new UriPatten("/debug/api/themes/");
    public static final UriPatten URI_PATTEN_API_LOGS = new UriPatten("/debug/api/logs/");
    public static final UriPatten URI_PATTEN_PAGE_INDEX = new UriPatten("/debug/index");
    private static final boolean IS_DEBUGGING_ENABLED;
    private static final Logger log = LoggerFactory.getLogger(Debugger.class);

    private final DebugAppender debugAppender;

    static {
        IS_DEBUGGING_ENABLED = ManagementFactory.getRuntimeMXBean().getInputArguments().contains("-Xdebug");
    }

    public Debugger() {
        this.debugAppender = new DebugAppender();
        this.debugAppender.attach();
    }

    public void serve(App app, HttpRequest request, HttpResponse response) {
        String uriWithoutAppContext = request.getUriWithoutAppContext();

        if (URI_PATTEN_API_PAGES.matches(uriWithoutAppContext)) {
            JsonObject jsonObject = new JsonObject();
            for (Map.Entry<String, Component> entry : app.getComponents().entrySet()) {
                JsonArray jsonArray = new JsonArray();
                entry.getValue().getPages().stream().forEach(page -> jsonArray.add(page.toString()));
                jsonObject.add(entry.getKey(), jsonArray);
            }
            response.setContent(STATUS_OK, jsonObject.toString(), CONTENT_TYPE_APPLICATION_JSON);
            return;
        }

        // TODO: 6/5/16 add an API to return layouts "/debug/api/layouts/"

        if (URI_PATTEN_API_FRAGMENTS.matches(uriWithoutAppContext)) {
            JsonArray jsonArray = new JsonArray();
            app.getFragments().values().stream().forEach(fragment -> jsonArray.add(fragment.toString()));
            response.setContent(STATUS_OK, jsonArray.toString(), CONTENT_TYPE_APPLICATION_JSON);
            return;
        }

        if (URI_PATTEN_API_THEMES.matches(uriWithoutAppContext)) {
            JsonArray jsonArray = new JsonArray();
            app.getThemes().values().stream().forEach(theme -> jsonArray.add(theme.toString()));
            response.setContent(STATUS_OK, jsonArray.toString(), CONTENT_TYPE_APPLICATION_JSON);
            return;
        }

        if (URI_PATTEN_API_LOGS.matches(uriWithoutAppContext)) {
            response.setContent(STATUS_OK, debugAppender.getMessagesAsJson(), CONTENT_TYPE_APPLICATION_JSON);
            return;
        }

        if (URI_PATTEN_PAGE_INDEX.matches(uriWithoutAppContext)) {
            InputStream resourceAsStream = this.getClass().getResourceAsStream("/apps/index.html");
            if (resourceAsStream == null) {
                log.error("Cannot find resource '/apps/index.html' in classpath.");
                response.setStatus(STATUS_NOT_FOUND);
                return;
            }
            try {
                String debugContent = IOUtils.toString(resourceAsStream, "UTF-8");
                response.setContent(STATUS_OK, IOUtils.toString(resourceAsStream, "UTF-8"), CONTENT_TYPE_TEXT_HTML);
            } catch (IOException e) {
                log.error("Cannot read string from input stream of '/apps/index.html' in classpath", e);
                response.setStatus(STATUS_INTERNAL_SERVER_ERROR);
            }
            return;
        }

        response.setStatus(STATUS_BAD_REQUEST); // This URI is not supported.
    }

    public static boolean isDebuggingEnabled() {
        return IS_DEBUGGING_ENABLED;
    }
}
