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

package org.wso2.carbon.uuf.handlebars.renderable;

import com.google.gson.Gson;
import jdk.nashorn.api.scripting.NashornScriptEngine;
import jdk.nashorn.api.scripting.NashornScriptEngineFactory;
import org.wso2.carbon.uuf.api.Placeholder;
import org.wso2.carbon.uuf.api.auth.Session;
import org.wso2.carbon.uuf.core.API;
import org.wso2.carbon.uuf.exception.UUFException;

import javax.script.ScriptEngine;
import javax.script.ScriptException;
import java.util.Map;

// TODO remove this SuppressWarnings
@SuppressWarnings("PackageAccessibility")
public class JSExecutable implements Executable {

    private static final NashornScriptEngineFactory SCRIPT_ENGINE_FACTORY = new NashornScriptEngineFactory();
    private static final String[] SCRIPT_ENGINE_ARGS = new String[]{"-strict", "--optimistic-types"};

    private final NashornScriptEngine engine;
    private final String scriptPath;
    private final String componentPath;
    private final Gson gson;

    public JSExecutable(String scriptSource, ClassLoader componentClassLoader) {
        this(scriptSource, componentClassLoader, null, null);
    }

    public JSExecutable(String scriptSource, ClassLoader componentClassLoader, String scriptPath,
                        String componentPath) {
        this.scriptPath = scriptPath;
        this.componentPath = componentPath;
        this.gson = new Gson();
        NashornScriptEngine engine = (NashornScriptEngine) SCRIPT_ENGINE_FACTORY.getScriptEngine(SCRIPT_ENGINE_ARGS,
                                                                                                 componentClassLoader);
        engine.put(ScriptEngine.FILENAME, this.scriptPath);
        try {
            engine.eval(scriptSource);
            // Even though 'NashornScriptEngineFactory.getParameter("THREADING")' returns null, NashornScriptEngine is
            // thread-safe. See http://stackoverflow.com/a/30159424
            this.engine = engine;
        } catch (ScriptException e) {
            throw new UUFException("An error occurred when evaluating the JavaScript file '" + this.scriptPath + "'.",
                                   e);
        }
    }

    public Object execute(Object context, API api) {
        try {
            return engine.invokeFunction("onRequest", context, new UUF(api, gson));
        } catch (ScriptException e) {
            throw new UUFException("An error occurred when executing the 'onRequest' function in JavaScript file '" +
                                           scriptPath + "' with context '" + context + "'.", e);
        } catch (NoSuchMethodException e) {
            throw new UUFException("Cannot find the 'onRequest' function in the JavaScript file '" + scriptPath + "'.",
                                   e);
        }
    }

    @Override
    public String toString() {
        return "{\"path\": \"" + scriptPath + "\"}";
    }

    public static class UUF {

        private final API api;
        private final Gson gson;

        private UUF(API api, Gson gson) {
            this.api = api;
            this.gson = gson;
        }

        public Object callOSGiService(String serviceClassName, String serviceMethodName, Object[] args) {
            return API.callOSGiService(serviceClassName, serviceMethodName, args);
        }

        public Map<String, Object> getOSGiServices(String serviceClassName) {
            return API.getOSGiServices(serviceClassName);
        }

        public void callMicroService() {
            API.callMicroService();
        }

        public void sendError(int status, String message) {
            API.sendError(status, message);
        }

        public void sendRedirect(String redirectUrl) {
            API.sendRedirect(redirectUrl);
        }

        public Session createSession(String userName) {
            return api.createSession(userName);
        }

        public Session getSession() {
            return api.getSession().orElse(null);
        }

        public boolean destroySession() {
            return api.destroySession();
        }

        public void setAppTheme(String themeName) {
            api.setAppTheme(themeName);
        }

        public String getAppTheme() {
            return api.getAppTheme().orElse(null);
        }

        public void sendToClient(String name, Object value) {
            String scriptTag = "<script type=\"text/javascript\">var " + name + "=" + gson.toJson(value) + ";</script>";
            api.getRequestLookup().addToPlaceholder(Placeholder.js, scriptTag);
        }

        @Override
        public String toString() {
            return "{\"callOSGiService\":\"function(serviceClassName, serviceMethodName, args)\", " +
                    "\"getOSGiServices\":\"function(serviceClassName)\", \"callMicroService\":\"function()\", " +
                    "\"sendError\":\"function(status, message)\", \"sendRedirect\":\"function(redirectUrl)\", " +
                    "\"createSession\":\"function(userName)\", \"getSession\":\"function()\", " +
                    "\"destroySession\":\"function()\", \"setAppTheme\":\"function(themeName)\", " +
                    "\"getAppTheme\":\"function()\", \"sendToClient\":\"function(name, value)\"}";
        }
    }
}
