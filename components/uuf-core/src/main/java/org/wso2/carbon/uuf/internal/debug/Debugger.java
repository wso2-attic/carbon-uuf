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

package org.wso2.carbon.uuf.internal.debug;

import com.google.gson.JsonArray;
import com.google.gson.JsonObject;
import com.google.gson.JsonParser;
import org.apache.commons.io.FilenameUtils;
import org.apache.commons.io.IOUtils;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import org.wso2.carbon.uuf.core.App;
import org.wso2.carbon.uuf.core.UriPatten;
import org.wso2.carbon.uuf.internal.io.util.MimeMapper;
import org.wso2.carbon.uuf.spi.HttpRequest;
import org.wso2.carbon.uuf.spi.HttpResponse;

import java.io.IOException;
import java.io.InputStream;
import java.lang.management.ManagementFactory;

import static org.wso2.carbon.uuf.spi.HttpResponse.CONTENT_TYPE_APPLICATION_JSON;
import static org.wso2.carbon.uuf.spi.HttpResponse.CONTENT_TYPE_WILDCARD;
import static org.wso2.carbon.uuf.spi.HttpResponse.STATUS_BAD_REQUEST;
import static org.wso2.carbon.uuf.spi.HttpResponse.STATUS_INTERNAL_SERVER_ERROR;
import static org.wso2.carbon.uuf.spi.HttpResponse.STATUS_NOT_FOUND;
import static org.wso2.carbon.uuf.spi.HttpResponse.STATUS_OK;

public class Debugger {

    private static final UriPatten URI_PATTEN_API_APP = new UriPatten("/debug/api/app/");
    private static final UriPatten URI_PATTEN_API_COMPONENTS = new UriPatten("/debug/api/components/");
    private static final UriPatten URI_PATTEN_API_PAGES = new UriPatten("/debug/api/pages/");
    private static final UriPatten URI_PATTEN_API_LAYOUTS = new UriPatten("/debug/api/layouts/");
    private static final UriPatten URI_PATTEN_API_FRAGMENTS = new UriPatten("/debug/api/fragments/");
    private static final UriPatten URI_PATTEN_API_THEMES = new UriPatten("/debug/api/themes/");
    private static final UriPatten URI_PATTEN_API_LOGS = new UriPatten("/debug/api/logs/");
    private static final UriPatten URI_PATTEN_PAGE_INDEX = new UriPatten("/debug/");
    private static final UriPatten URI_PATTEN_RESOURCES = new UriPatten("/debug/{+resource}");
    private final static JsonParser JSON_PARSER = new JsonParser();
    private static final Logger log = LoggerFactory.getLogger(Debugger.class);
    private static final boolean IS_DEBUGGING_ENABLED;

    static {
        IS_DEBUGGING_ENABLED = ManagementFactory.getRuntimeMXBean().getInputArguments().contains("-Xdebug");
    }

    // TODO: 12/07/2016 uncomment this once osgi issue solved for DebugAppender
    //private final DebugAppender debugAppender;

    public Debugger() {
        // TODO: 12/07/2016 uncomment this once osgi issue solved for DebugAppender
        //this.debugAppender = new DebugAppender("debugger", "");
        //this.debugAppender.attach();
    }

    public void serve(App app, HttpRequest request, HttpResponse response) {
        String uriWithoutContextPath = request.getUriWithoutContextPath();
        DebugConnector debugConnector = new DebugConnector(app);

        if (URI_PATTEN_API_APP.matches(uriWithoutContextPath)) {
            JsonObject appContent = new JsonObject();
            appContent.add(app.getContextPath(), JSON_PARSER.parse(app.toString()));
            response.setContent(appContent.toString(), CONTENT_TYPE_APPLICATION_JSON);
            return;
        }

        if (URI_PATTEN_API_COMPONENTS.matches(uriWithoutContextPath)) {
            JsonArray components = new JsonArray();
            debugConnector.getComponents().forEach(
                    (component -> components.add(JSON_PARSER.parse(component.toString()))));
            response.setContent(components.toString(), CONTENT_TYPE_APPLICATION_JSON);
            return;
        }

        if (URI_PATTEN_API_PAGES.matches(uriWithoutContextPath)) {
            JsonObject content = new JsonObject();
            debugConnector.getComponents().forEach(
                    (component) -> {
                        JsonArray pages = new JsonArray();
                        component.getPages().forEach(page -> pages.add(JSON_PARSER.parse(page.toString())));
                        content.add(component.getContextPath(), pages);
                    }
            );
            response.setContent(content.toString(), CONTENT_TYPE_APPLICATION_JSON);
            return;
        }

        if (URI_PATTEN_API_LAYOUTS.matches(uriWithoutContextPath)) {
            JsonArray jsonArray = new JsonArray();
            debugConnector.getLayouts().forEach(layout -> jsonArray.add(JSON_PARSER.parse(layout.toString())));
            response.setContent(jsonArray.toString(), CONTENT_TYPE_APPLICATION_JSON);
            return;
        }

        if (URI_PATTEN_API_FRAGMENTS.matches(uriWithoutContextPath)) {
            JsonArray jsonArray = new JsonArray();
            debugConnector.getFragments().forEach(fragment -> jsonArray.add(JSON_PARSER.parse(fragment.toString())));
            response.setContent(jsonArray.toString(), CONTENT_TYPE_APPLICATION_JSON);
            return;
        }

        if (URI_PATTEN_API_THEMES.matches(uriWithoutContextPath)) {
            JsonArray jsonArray = new JsonArray();
            debugConnector.getThemes().forEach(theme -> jsonArray.add(JSON_PARSER.parse(theme.toString())));
            response.setContent(jsonArray.toString(), CONTENT_TYPE_APPLICATION_JSON);
            return;
        }

        // TODO: 12/07/2016 uncomment this once osgi issue solved for DebugAppender
        // if (URI_PATTEN_API_LOGS.matches(uriWithoutContextPath)) {
        //      response.setContent(STATUS_OK, debugAppender.getMessagesAsJson(), CONTENT_TYPE_APPLICATION_JSON);
        //      return;
        // }

        if (URI_PATTEN_PAGE_INDEX.matches(uriWithoutContextPath) ||
                URI_PATTEN_RESOURCES.matches(uriWithoutContextPath)) {
            char tailChar = uriWithoutContextPath.charAt(uriWithoutContextPath.length() - 1);
            uriWithoutContextPath = (tailChar == '/') ? uriWithoutContextPath + "index.html" : uriWithoutContextPath;
            String resourcePath = "/apps" + uriWithoutContextPath;
            InputStream resourceAsStream = this.getClass().getResourceAsStream(resourcePath);
            if (resourceAsStream == null) {
                log.error("Cannot find resource '" + resourcePath + "' in classpath.");
                response.setStatus(STATUS_NOT_FOUND);
                return;
            }
            try {
                String debugContent = IOUtils.toString(resourceAsStream, "UTF-8");
                String extensionFromUri = FilenameUtils.getExtension(uriWithoutContextPath);
                response.setContent(STATUS_OK, debugContent,
                                    MimeMapper.getMimeType(extensionFromUri).orElse(CONTENT_TYPE_WILDCARD));
            } catch (IOException e) {
                log.error("Cannot read string from input stream of '" + resourcePath + "' in classpath", e);
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
