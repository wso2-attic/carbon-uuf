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

package org.wso2.carbon.uuf.handlebars.renderable.js;

import com.google.gson.Gson;
import org.slf4j.Logger;
import org.wso2.carbon.uuf.api.Placeholder;
import org.wso2.carbon.uuf.core.API;
import org.wso2.carbon.uuf.exception.UUFException;

import javax.script.ScriptEngine;
import javax.script.ScriptException;
import java.io.IOException;
import java.nio.charset.StandardCharsets;
import java.nio.file.Files;
import java.nio.file.Path;
import java.nio.file.Paths;

public class JSFunctionsImpl {

    private static final CallOSGiServiceFunction CALL_OSGI_SERVICE_FUNCTION;
    private static final GetOSGiServicesFunction GET_OSGI_SERVICES_FUNCTION;
    private static final CallMicroServiceFunction CALL_MICRO_SERVICE_FUNCTION;
    private static final SendErrorFunction SEND_ERROR_FUNCTION;
    private static final SendRedirectFunction SEND_REDIRECT_FUNCTION;
    private static final Gson GSON;

    private final API api;
    private CreateSessionFunction createSessionFunction;
    private GetSessionFunction getSessionFunction;
    private DestroySessionFunction destroySessionFunction;
    private SetAppThemeFunction setAppThemeFunction;
    private GetAppThemeFunction getAppThemeFunction;
    private SendToClientFunction sendToClientFunction;

    static {
        CALL_OSGI_SERVICE_FUNCTION = API::callOSGiService;
        GET_OSGI_SERVICES_FUNCTION = API::getOSGiServices;
        CALL_MICRO_SERVICE_FUNCTION = API::callMicroService;
        SEND_ERROR_FUNCTION = API::sendError;
        SEND_REDIRECT_FUNCTION = API::sendRedirect;
        GSON = new Gson();
    }

    public JSFunctionsImpl(API api) {
        this.api = api;
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
                throw new UUFException("Cannot read content of JavaScript module '" + moduleName +
                                               "' in component module directory '" + modulesDirPath + ".", e);
            } catch (ScriptException e) {
                throw new UUFException("An error occurred while evaluating the JavaScript module '" + moduleName +
                                               "' in component module directory '" + modulesDirPath + ".", e);
            }
        };
    }

    public static LogFunction getLogFunction(Logger log) {
        return new LogFunction() {
            @Override
            public void call(Object... args) {
                if (args.length == 1) {
                    log.info(getLogMessage(args[0]));
                } else if (args.length == 2) {
                    String message = getLogMessage(args[1]);
                    switch (getLogLevel(args[0])) {
                        case INFO:
                            log.info(message);
                            break;
                        case DEBUG:
                            log.debug(message);
                            break;
                        case TRACE:
                            log.trace(message);
                            break;
                        case WARN:
                            log.warn(message);
                            break;
                        case ERROR:
                            log.error(message);
                    }
                } else {
                    throw new IllegalArgumentException("Incorrect number of arguments for 'log' function.");
                }
            }
        };
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

    public SetAppThemeFunction getSetAppThemeFunction() {
        if (setAppThemeFunction == null) {
            setAppThemeFunction = api::setAppTheme;
        }
        return setAppThemeFunction;
    }

    public GetAppThemeFunction getGetAppThemeFunction() {
        if (getAppThemeFunction == null) {
            getAppThemeFunction = () -> api.getAppTheme().orElse(null);
        }
        return getAppThemeFunction;
    }

    public SendToClientFunction getSendToClientFunction() {
        if (sendToClientFunction == null) {
            sendToClientFunction = (name, value) -> {
                String scriptTag = "<script type=\"text/javascript\">var " + name + "=" + GSON.toJson(value) +
                        ";</script>";
                api.getRequestLookup().addToPlaceholder(Placeholder.js, scriptTag);
            };
        }
        return sendToClientFunction;
    }
}
