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

package org.wso2.carbon.uuf.handlebars;

import jdk.nashorn.api.scripting.NashornScriptEngine;
import jdk.nashorn.api.scripting.NashornScriptEngineFactory;
import org.wso2.carbon.uuf.core.API;
import org.wso2.carbon.uuf.core.exception.UUFException;

import javax.script.ScriptException;
import java.util.Map;
import java.util.Optional;

// TODO remove this SuppressWarnings
@SuppressWarnings("PackageAccessibility")
public class JSExecutable implements Executable {

    private static final NashornScriptEngineFactory SCRIPT_ENGINE_FACTORY = new NashornScriptEngineFactory();
    private static final String[] SCRIPT_ENGINE_ARGS = new String[]{"-strict"};

    private final Optional<String> scriptPath;
    private final NashornScriptEngine engine;

    public JSExecutable(String scriptSource, ClassLoader componentClassLoader, Optional<String> scriptPath) {
        this.scriptPath = scriptPath;
        if (scriptPath.isPresent()) {
            // Append script file name for debugging purposes.
            scriptSource = scriptSource + "//@ sourceURL=" + getPath();
        }

        NashornScriptEngine engine = (NashornScriptEngine) SCRIPT_ENGINE_FACTORY.getScriptEngine(SCRIPT_ENGINE_ARGS,
                                                                                                 componentClassLoader);
        engine.put("callOSGiService", (FunctionCallOSGiService) API::callOSGiService);
        engine.put("getOSGiServices", (FunctionGetOSGiServices) API::getOSGiServices);
        engine.put("callMicroService", (FunctionCallMicroService) API::callMicroService);
        engine.put("sendError", (FunctionSendError) API::sendError);
        engine.put("sendRedirect", (FunctionSendRedirect) API::sendRedirect);
        try {
            engine.eval(scriptSource);
            this.engine = engine;
        } catch (ScriptException e) {
            throw new UUFException("An error occurred when evaluating the JavaScript file '" + getPath() + "'.", e);
        }
    }

    private String getPath() {
        return scriptPath.orElse("\"<in-memory-script>\"");
    }

    public Object execute(Object context, API api) {
        engine.put("createSession", (FunctionCreateSession) api::createSession);
        engine.put("setTheme", (FunctionSetTheme) api::setTheme);
        try {
            return engine.invokeFunction("onRequest", context);
        } catch (ScriptException e) {
            throw new UUFException("An error occurred when executing the 'onRequest' function in JavaScript file '" +
                                           getPath() + "' with context '" + context + "'.", e);
        } catch (NoSuchMethodException e) {
            throw new UUFException("Cannot find the 'onRequest' function in the JavaScript file '" + getPath() + "'.",
                                   e);
        }
    }

    @Override
    public String toString() {
        return "{\"path\": \"" + getPath() + "\"}";
    }

    @FunctionalInterface
    private interface FunctionCallOSGiService {
        @SuppressWarnings("unused")
        Object call(String serviceClassName, String serviceMethodName, Object... args);
    }

    @FunctionalInterface
    private interface FunctionGetOSGiServices {
        @SuppressWarnings("unused")
        Map<String, Object> call(String serviceClassName);
    }

    @FunctionalInterface
    private interface FunctionCallMicroService {
        @SuppressWarnings("unused")
        void call();
    }

    @FunctionalInterface
    private interface FunctionCreateSession {
        @SuppressWarnings("unused")
        void call();
    }

    @FunctionalInterface
    private interface FunctionSendError {
        @SuppressWarnings("unused")
        void call(int status, String message);
    }

    @FunctionalInterface
    private interface FunctionSendRedirect {
        @SuppressWarnings("unused")
        void call(String redirectUrl);
    }

    @FunctionalInterface
    private interface FunctionSetTheme {
        @SuppressWarnings("unused")
        void call(String name);
    }
}
