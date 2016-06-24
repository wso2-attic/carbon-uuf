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

import jdk.nashorn.api.scripting.NashornScriptEngine;
import jdk.nashorn.api.scripting.NashornScriptEngineFactory;
import org.wso2.carbon.uuf.core.API;
import org.wso2.carbon.uuf.exception.UUFException;
import org.wso2.carbon.uuf.handlebars.renderable.Executable;

import javax.script.ScriptContext;
import javax.script.ScriptEngine;
import javax.script.ScriptException;
import javax.script.SimpleBindings;

// TODO remove this SuppressWarnings
@SuppressWarnings("PackageAccessibility")
public class JSExecutable implements Executable {

    private static final NashornScriptEngineFactory SCRIPT_ENGINE_FACTORY = new NashornScriptEngineFactory();
    private static final String[] SCRIPT_ENGINE_ARGS = new String[]{"-strict", "--optimistic-types"};

    private final NashornScriptEngine engine;
    private final UUFBindings engineBindings;
    private final String scriptPath;
    private final String componentPath;

    public JSExecutable(String scriptSource, ClassLoader componentClassLoader) {
        this(scriptSource, componentClassLoader, null, null);
    }

    public JSExecutable(String scriptSource, ClassLoader componentClassLoader, String scriptPath,
                        String componentPath) {
        this.scriptPath = scriptPath;
        this.componentPath = componentPath;
        NashornScriptEngine engine = (NashornScriptEngine) SCRIPT_ENGINE_FACTORY.getScriptEngine(SCRIPT_ENGINE_ARGS,
                                                                                                 componentClassLoader);
        this.engineBindings = new UUFBindings();
        engineBindings.put(ScriptEngine.FILENAME, this.scriptPath);
        engineBindings.put("callOSGiService", JSFunctionProvider.getCallOsgiServiceFunction());
        engineBindings.put("getOSGiServices", JSFunctionProvider.getGetOsgiServicesFunction());
        engineBindings.put("callMicroService", JSFunctionProvider.getCallMicroServiceFunction());
        engineBindings.put("sendError", JSFunctionProvider.getSendErrorFunction());
        engineBindings.put("sendRedirect", JSFunctionProvider.getSendRedirectFunction());
        engine.setBindings(engineBindings, ScriptContext.ENGINE_SCOPE);
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
            engineBindings.setJSFunctionProvider(new JSFunctionProvider(api));
            return engine.invokeFunction("onRequest", context);
        } catch (ScriptException e) {
            throw new UUFException("An error occurred when executing the 'onRequest' function in JavaScript file '" +
                                           scriptPath + "' with context '" + context + "'.", e);
        } catch (NoSuchMethodException e) {
            throw new UUFException("Cannot find the 'onRequest' function in the JavaScript file '" + scriptPath + "'.",
                                   e);
        } finally {
            engineBindings.removeJSFunctionProvider();
        }
    }

    @Override
    public String toString() {
        return "{\"path\": \"" + scriptPath + "\"}";
    }

    public static class UUFBindings extends SimpleBindings {

        private static final String KEY_CREATE_SESSION = "createSession";
        private static final String KEY_GET_SESSION = "getSession";
        private static final String KEY_DESTROY_SESSION = "destroySession";
        private static final String KEY_SET_APP_THEME = "setAppTheme";
        private static final String KEY_GET_APP_THEME = "getAppTheme";
        private static final String KEY_SEND_TO_CLIENT = "sendToClient";

        private final ThreadLocal<JSFunctionProvider> threadLocalFunctionProvider = new ThreadLocal<>();

        public void setJSFunctionProvider(JSFunctionProvider functionProvider) {
            threadLocalFunctionProvider.set(functionProvider);
        }

        public void removeJSFunctionProvider() {
            threadLocalFunctionProvider.remove();
        }

        @Override
        public Object get(Object key) {
            if (!(key instanceof String)) {
                return super.get(key);
            }
            switch ((String) key) {
                case KEY_CREATE_SESSION:
                    return threadLocalFunctionProvider.get().getCreateSessionFunction();
                case KEY_GET_SESSION:
                    return threadLocalFunctionProvider.get().getGetSessionFunction();
                case KEY_DESTROY_SESSION:
                    return threadLocalFunctionProvider.get().getDestroySessionFunction();
                case KEY_SET_APP_THEME:
                    return threadLocalFunctionProvider.get().getSetAppThemeFunction();
                case KEY_GET_APP_THEME:
                    return threadLocalFunctionProvider.get().getGetAppThemeFunction();
                case KEY_SEND_TO_CLIENT:
                    return threadLocalFunctionProvider.get().getSendToClientFunction();
                default:
                    return super.get(key);
            }
        }

        @Override
        public boolean containsKey(Object key) {
            if (!(key instanceof String)) {
                return super.containsKey(key);
            }
            switch ((String) key) {
                case KEY_CREATE_SESSION:
                case KEY_GET_SESSION:
                case KEY_DESTROY_SESSION:
                case KEY_SET_APP_THEME:
                case KEY_GET_APP_THEME:
                case KEY_SEND_TO_CLIENT:
                    return true;
                default:
                    return super.containsKey(key);
            }
        }
    }
}
