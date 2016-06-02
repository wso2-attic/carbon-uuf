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
import org.wso2.carbon.uuf.api.auth.Session;
import org.wso2.carbon.uuf.core.API;
import org.wso2.carbon.uuf.exception.UUFException;

import javax.script.ScriptException;
import java.util.Map;

// TODO remove this SuppressWarnings
@SuppressWarnings("PackageAccessibility")
public class JSExecutable implements Executable {

    private static final NashornScriptEngineFactory SCRIPT_ENGINE_FACTORY = new NashornScriptEngineFactory();
    private static final String[] SCRIPT_ENGINE_ARGS = new String[]{"-strict"};

    private final String scriptPath;
    private final NashornScriptEngine engine;

    public JSExecutable(String scriptSource, ClassLoader componentClassLoader) {
        this(scriptSource, null, componentClassLoader);
    }

    public JSExecutable(String scriptSource, String scriptPath, ClassLoader componentClassLoader) {
        this.scriptPath = scriptPath;
        scriptSource = scriptSource + "//@ sourceURL=" + scriptPath; // Append script file path for debugging purposes.

        NashornScriptEngine engine = (NashornScriptEngine) SCRIPT_ENGINE_FACTORY.getScriptEngine(SCRIPT_ENGINE_ARGS,
                                                                                                 componentClassLoader);
        engine.put("callOSGiService", (CallOSGiService) API::callOSGiService);
        engine.put("getOSGiServices", (GetOSGiServices) API::getOSGiServices);
        engine.put("callMicroService", (CallMicroService) API::callMicroService);
        engine.put("sendError", (SendError) API::sendError);
        engine.put("sendRedirect", (SendRedirect) API::sendRedirect);
        try {
            engine.eval(scriptSource);
            this.engine = engine;
        } catch (ScriptException e) {
            throw new UUFException("An error occurred when evaluating the JavaScript file '" + getPath() + "'.", e);
        }
    }

    private String getPath() {
        return scriptPath;
    }

    public Object execute(Object context, API api) {
        engine.put("createSession", (CreateSession) api::createSession);
        engine.put("getSession", (GetSession) () -> api.getSession().orElse(null));
        engine.put("destroySession", (DestroySession) api::destroySession);
        engine.put("setAppTheme", (SetTheme) api::setAppTheme);
        engine.put("getAppTheme", (GetTheme) () -> api.getAppTheme().orElse(null));
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
        return "{\"path\": \"" + scriptPath + "\"}";
    }

    @FunctionalInterface
    public interface CallOSGiService {

        @SuppressWarnings("unused")
        Object call(String serviceClassName, String serviceMethodName, Object... args);
    }

    @FunctionalInterface
    public interface GetOSGiServices {

        @SuppressWarnings("unused")
        Map<String, Object> call(String serviceClassName);
    }

    @FunctionalInterface
    public interface CallMicroService {

        @SuppressWarnings("unused")
        void call();
    }

    @FunctionalInterface
    public interface CreateSession {

        @SuppressWarnings("unused")
        Session call(String userName);
    }

    @FunctionalInterface
    public interface GetSession {

        @SuppressWarnings("unused")
        Session call();
    }

    @FunctionalInterface
    public interface DestroySession {

        @SuppressWarnings("unused")
        boolean call();
    }

    @FunctionalInterface
    public interface SendError {

        @SuppressWarnings("unused")
        void call(int status, String message);
    }

    @FunctionalInterface
    public interface SendRedirect {

        @SuppressWarnings("unused")
        void call(String redirectUrl);
    }

    @FunctionalInterface
    public interface SetTheme {

        @SuppressWarnings("unused")
        void call(String name);
    }

    @FunctionalInterface
    public interface GetTheme {

        @SuppressWarnings("unused")
        String call();
    }
}
