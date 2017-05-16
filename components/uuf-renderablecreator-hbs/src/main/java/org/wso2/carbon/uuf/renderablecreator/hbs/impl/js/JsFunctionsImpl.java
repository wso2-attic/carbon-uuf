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

package org.wso2.carbon.uuf.renderablecreator.hbs.impl.js;

import com.github.jknack.handlebars.Handlebars;
import org.wso2.carbon.uuf.api.Placeholder;
import org.wso2.carbon.uuf.core.API;
import org.wso2.carbon.uuf.core.Lookup;
import org.wso2.carbon.uuf.core.RequestLookup;
import org.wso2.carbon.uuf.exception.FileOperationException;
import org.wso2.carbon.uuf.exception.UUFException;
import org.wso2.carbon.uuf.renderablecreator.hbs.core.js.CallMicroServiceFunction;
import org.wso2.carbon.uuf.renderablecreator.hbs.core.js.CallOSGiServiceFunction;
import org.wso2.carbon.uuf.renderablecreator.hbs.core.js.CreateSessionFunction;
import org.wso2.carbon.uuf.renderablecreator.hbs.core.js.DestroySessionFunction;
import org.wso2.carbon.uuf.renderablecreator.hbs.core.js.GetOSGiServicesFunction;
import org.wso2.carbon.uuf.renderablecreator.hbs.core.js.GetSessionFunction;
import org.wso2.carbon.uuf.renderablecreator.hbs.core.js.I18nFunction;
import org.wso2.carbon.uuf.renderablecreator.hbs.core.js.ModuleFunction;
import org.wso2.carbon.uuf.renderablecreator.hbs.core.js.SendErrorFunction;
import org.wso2.carbon.uuf.renderablecreator.hbs.core.js.SendRedirectFunction;
import org.wso2.carbon.uuf.renderablecreator.hbs.core.js.SendToClientFunction;
import org.wso2.carbon.uuf.renderablecreator.hbs.internal.serialize.JsonSerializer;
import org.wso2.carbon.uuf.spi.HttpRequest;

import java.io.IOException;
import java.nio.charset.StandardCharsets;
import java.nio.file.Files;
import java.nio.file.Path;
import java.nio.file.Paths;
import java.util.Locale;
import javax.script.ScriptEngine;
import javax.script.ScriptException;

public class JsFunctionsImpl {

    private static final CallOSGiServiceFunction CALL_OSGI_SERVICE_FUNCTION;
    private static final GetOSGiServicesFunction GET_OSGI_SERVICES_FUNCTION;
    private static final CallMicroServiceFunction CALL_MICRO_SERVICE_FUNCTION;
    private static final SendErrorFunction SEND_ERROR_FUNCTION;
    private static final SendRedirectFunction SEND_REDIRECT_FUNCTION;

    private final API api;
    private final Lookup lookup;
    private final RequestLookup requestLookup;
    private CreateSessionFunction createSessionFunction;
    private GetSessionFunction getSessionFunction;
    private DestroySessionFunction destroySessionFunction;
    private SendToClientFunction sendToClientFunction;
    private I18nFunction i18nFunction;

    static {
        CALL_OSGI_SERVICE_FUNCTION = API::callOSGiService;
        GET_OSGI_SERVICES_FUNCTION = API::getOSGiServices;
        CALL_MICRO_SERVICE_FUNCTION = API::callMicroService;
        SEND_ERROR_FUNCTION = API::sendError;
        SEND_REDIRECT_FUNCTION = API::sendRedirect;
    }

    public JsFunctionsImpl(API api, Lookup lookup, RequestLookup requestLookup) {
        this.api = api;
        this.lookup = lookup;
        this.requestLookup = requestLookup;
    }

    public static CallOSGiServiceFunction getCallOsgiServiceFunction() {
        return CALL_OSGI_SERVICE_FUNCTION;
    }

    public static GetOSGiServicesFunction getGetOsgiServicesFunction() {
        return GET_OSGI_SERVICES_FUNCTION;
    }

    public static CallMicroServiceFunction getCallMicroServiceFunction() {
        return CALL_MICRO_SERVICE_FUNCTION;
    }

    public static SendErrorFunction getSendErrorFunction() {
        return SEND_ERROR_FUNCTION;
    }

    public static SendRedirectFunction getSendRedirectFunction() {
        return SEND_REDIRECT_FUNCTION;
    }

    public static ModuleFunction getModuleFunction(String componentPath, ScriptEngine engine) {
        return moduleName -> {
            Path modulesDirPath = Paths.get(componentPath, "modules");
            Path jsFilePath = modulesDirPath.resolve(moduleName + ".js");
            if (!Files.exists(jsFilePath)) {
                throw new IllegalArgumentException(
                        "JavaScript module '" + moduleName + "' does not exists in component module directory '" +
                                modulesDirPath + "'.");
            }

            try {
                String content = new String(Files.readAllBytes(jsFilePath), StandardCharsets.UTF_8);
                engine.eval(content);
            } catch (IOException e) {
                throw new FileOperationException("Cannot read content of JavaScript module '" + moduleName +
                                                         "' in component module directory '" + modulesDirPath + ".", e);
            } catch (ScriptException e) {
                throw new UUFException("An error occurred while evaluating the JavaScript module '" + moduleName +
                                               "' in component module directory '" + modulesDirPath + ".", e);
            }
        };
    }

    public static LoggerObject getLoggerObject(String name) {
        return new LoggerObject(name);
    }

    public CreateSessionFunction getCreateSessionFunction() {
        if (createSessionFunction == null) {
            createSessionFunction = api::createSession;
        }
        return createSessionFunction;
    }

    public GetSessionFunction getGetSessionFunction() {
        if (getSessionFunction == null) {
            getSessionFunction = () -> api.getSession().orElse(null);
        }
        return getSessionFunction;
    }

    public DestroySessionFunction getDestroySessionFunction() {
        if (destroySessionFunction == null) {
            destroySessionFunction = api::destroySession;
        }
        return destroySessionFunction;
    }

    public SendToClientFunction getSendToClientFunction() {
        if (sendToClientFunction == null) {
            sendToClientFunction = (name, values) -> {
                String scriptTag = "<script type=\"text/javascript\">var " + Handlebars.Utils.escapeExpression(name) +
                        "=" + JsonSerializer.toSafeJson(values[0]) +
                        ";</script>";
                api.getRequestLookup().addToPlaceholder(
                        isHeadJsPlaceholder(values) ? Placeholder.headJs : Placeholder.js, scriptTag);
            };
        }
        return sendToClientFunction;
    }

    public I18nFunction getI18nFunction() {
        if (i18nFunction == null) {
            i18nFunction = (String messageKey, String... messageParams) -> {
                Locale locale;

                String headerLocale = requestLookup.getRequest().getHeaders().get(HttpRequest.HEADER_ACCEPT_LANGUAGE);
                locale = lookup.getI18nResources().getLocale(headerLocale);
                if (locale == null) {
                    // Seems like request doesn't carry a locale.
                    // So let's check whether a default locale is configured in the configuration.
                    Object defaultLocale = lookup.getConfiguration().other().get("defaultLocale");
                    if ((defaultLocale instanceof String) && !defaultLocale.toString().isEmpty()) {
                        locale = Locale.forLanguageTag(defaultLocale.toString());
                    } else {
                        // Since there is no other way to compute the locale, let's fall back to 'en'.
                        locale = Locale.ENGLISH;
                    }
                }

                return lookup.getI18nResources().getMessage(locale, messageKey, messageParams, messageKey);
            };
        }
        return i18nFunction;
    }

    private boolean isHeadJsPlaceholder(Object[] values) {
        // this method check whether the user wants to push javascript into HeadJs placeholder.
        // argument length is one means that, user haven't passed any placeholder.
        // argument length two means that user passed a placeholder. so are checking pass argument value is HeadJs.
        return values.length == 2 && Placeholder.headJs.name().equalsIgnoreCase((String) values[1]);
    }
}
