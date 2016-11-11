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

import com.google.gson.Gson;
import com.google.gson.GsonBuilder;
import com.google.gson.JsonArray;
import com.google.gson.JsonElement;
import com.google.gson.JsonNull;
import com.google.gson.JsonObject;
import com.google.gson.JsonSerializationContext;
import com.google.gson.JsonSerializer;
import jdk.nashorn.api.scripting.ScriptObjectMirror;
import org.wso2.carbon.uuf.api.Placeholder;
import org.wso2.carbon.uuf.core.API;
import org.wso2.carbon.uuf.exception.FileOperationException;
import org.wso2.carbon.uuf.exception.UUFException;
import org.wso2.carbon.uuf.renderablecreator.hbs.core.js.CallMicroServiceFunction;
import org.wso2.carbon.uuf.renderablecreator.hbs.core.js.CallOSGiServiceFunction;
import org.wso2.carbon.uuf.renderablecreator.hbs.core.js.CreateSessionFunction;
import org.wso2.carbon.uuf.renderablecreator.hbs.core.js.DestroySessionFunction;
import org.wso2.carbon.uuf.renderablecreator.hbs.core.js.GetOSGiServicesFunction;
import org.wso2.carbon.uuf.renderablecreator.hbs.core.js.GetSessionFunction;
import org.wso2.carbon.uuf.renderablecreator.hbs.core.js.ModuleFunction;
import org.wso2.carbon.uuf.renderablecreator.hbs.core.js.SendErrorFunction;
import org.wso2.carbon.uuf.renderablecreator.hbs.core.js.SendRedirectFunction;
import org.wso2.carbon.uuf.renderablecreator.hbs.core.js.SendToClientFunction;

import javax.script.ScriptEngine;
import javax.script.ScriptException;
import java.io.IOException;
import java.lang.reflect.Type;
import java.nio.charset.StandardCharsets;
import java.nio.file.Files;
import java.nio.file.Path;
import java.nio.file.Paths;

public class JsFunctionsImpl {

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
    private SendToClientFunction sendToClientFunction;

    static {
        CALL_OSGI_SERVICE_FUNCTION = API::callOSGiService;
        GET_OSGI_SERVICES_FUNCTION = API::getOSGiServices;
        CALL_MICRO_SERVICE_FUNCTION = API::callMicroService;
        SEND_ERROR_FUNCTION = API::sendError;
        SEND_REDIRECT_FUNCTION = API::sendRedirect;
        GSON = new GsonBuilder().registerTypeAdapter(ScriptObjectMirror.class,
                new ScriptObjectMirrorSerializer()).create();
    }

    public JsFunctionsImpl(API api) {
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
            sendToClientFunction = (name, value) -> {
                String scriptTag = "<script type=\"text/javascript\">var " + name + "=" + GSON.toJson(value) +
                        ";</script>";
                api.getRequestLookup().addToPlaceholder(Placeholder.js, scriptTag);
            };
        }
        return sendToClientFunction;
    }

    /**
     * This ScriptObjectMirrorSerializer class is needed to build the {@link Gson} instance with JsonSerializer that
     * understands complex (eg - having json arrays and maps) json structure created using ScriptObjectMirror
     * elements. This is needed to properly serialize sub elements of ScriptObjectMirror when sendToClient function
     * is invoked with a complex js object consisting of arrays and maps. etc.
     */
    private static class ScriptObjectMirrorSerializer implements JsonSerializer<ScriptObjectMirror> {

        @Override
        public JsonElement serialize(ScriptObjectMirror jsObj, Type type,
                                     JsonSerializationContext serializationContext) {
            return serialize(jsObj, serializationContext);
        }

        private JsonElement serialize(ScriptObjectMirror jsObj, JsonSerializationContext serializationContext) {
            if (jsObj == null) {
                return JsonNull.INSTANCE;
            }
            if (jsObj.isFunction()) {
                throw new UUFException("Cannot send java script functions from sendToClient function call");
            }
            if (jsObj.isArray()) {
                JsonArray jsonArray = new JsonArray();
                for (Object item : jsObj.values()) {
                    if (item instanceof ScriptObjectMirror) {
                        jsonArray.add(serialize((ScriptObjectMirror) item, serializationContext));
                    } else {
                        jsonArray.add(serializationContext.serialize(item));
                    }
                }
                return jsonArray;
            }
            if (jsObj.isEmpty()) {
                return new JsonObject();
            } else {
                JsonObject jsonObject = new JsonObject();
                for (String key : jsObj.getOwnKeys(true)) {
                    Object member = jsObj.getMember(key);
                    if (member instanceof ScriptObjectMirror) {
                        jsonObject.add(key, serialize((ScriptObjectMirror) member, serializationContext));
                    } else {
                        jsonObject.add(key, serializationContext.serialize(member));
                    }
                }
                return jsonObject;
            }
        }
    }
}
