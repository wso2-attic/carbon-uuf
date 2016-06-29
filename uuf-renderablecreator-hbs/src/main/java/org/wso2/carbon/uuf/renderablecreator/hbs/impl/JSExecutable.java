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

package org.wso2.carbon.uuf.renderablecreator.hbs.impl;

import jdk.nashorn.api.scripting.NashornScriptEngine;
import jdk.nashorn.api.scripting.NashornScriptEngineFactory;
import org.slf4j.LoggerFactory;
import org.wso2.carbon.uuf.core.API;
import org.wso2.carbon.uuf.exception.UUFException;
import org.wso2.carbon.uuf.renderablecreator.hbs.core.Executable;
import org.wso2.carbon.uuf.renderablecreator.hbs.core.js.CallMicroServiceFunction;
import org.wso2.carbon.uuf.renderablecreator.hbs.core.js.CallOSGiServiceFunction;
import org.wso2.carbon.uuf.renderablecreator.hbs.core.js.CreateSessionFunction;
import org.wso2.carbon.uuf.renderablecreator.hbs.core.js.DestroySessionFunction;
import org.wso2.carbon.uuf.renderablecreator.hbs.core.js.GetAppThemeFunction;
import org.wso2.carbon.uuf.renderablecreator.hbs.core.js.GetOSGiServicesFunction;
import org.wso2.carbon.uuf.renderablecreator.hbs.core.js.GetSessionFunction;
import org.wso2.carbon.uuf.renderablecreator.hbs.impl.js.JSFunctionsImpl;
import org.wso2.carbon.uuf.renderablecreator.hbs.core.js.LogFunction;
import org.wso2.carbon.uuf.renderablecreator.hbs.core.js.ModuleFunction;
import org.wso2.carbon.uuf.renderablecreator.hbs.core.js.SendErrorFunction;
import org.wso2.carbon.uuf.renderablecreator.hbs.core.js.SendRedirectFunction;
import org.wso2.carbon.uuf.renderablecreator.hbs.core.js.SendToClientFunction;
import org.wso2.carbon.uuf.renderablecreator.hbs.core.js.SetAppThemeFunction;

import javax.script.ScriptContext;
import javax.script.ScriptEngine;
import javax.script.ScriptException;
import javax.script.SimpleBindings;
import java.util.Map;
import java.util.Objects;

// TODO remove this SuppressWarnings
@SuppressWarnings("PackageAccessibility")
public class JSExecutable implements Executable {

    private static final NashornScriptEngineFactory SCRIPT_ENGINE_FACTORY = new NashornScriptEngineFactory();
    private static final String[] SCRIPT_ENGINE_ARGS = new String[]{"-strict", "--optimistic-types"};

    private final NashornScriptEngine engine;
    private final UUFBindings engineBindings;
    private final String scriptPath;

    public JSExecutable(String scriptSource, ClassLoader componentClassLoader) {
        this(scriptSource, componentClassLoader, null, null);
    }

    public JSExecutable(String scriptSource, ClassLoader componentClassLoader, String scriptPath,
                        String componentPath) {
        this.scriptPath = scriptPath;
        NashornScriptEngine engine = (NashornScriptEngine) SCRIPT_ENGINE_FACTORY.getScriptEngine(SCRIPT_ENGINE_ARGS,
                                                                                                 componentClassLoader);
        UUFBindings engineBindings = new UUFBindings();
        engine.setBindings(engineBindings, ScriptContext.ENGINE_SCOPE);

        engineBindings.put(ScriptEngine.FILENAME, this.scriptPath);
        engineBindings.put(ModuleFunction.NAME, JSFunctionsImpl.getModuleFunction(componentPath, engine));
        try {
            engine.eval(scriptSource);
        } catch (ScriptException e) {
            throw new UUFException("An error occurred while evaluating the JavaScript file '" + this.scriptPath + "'.",
                                   e);
        }
        engineBindings.remove(ModuleFunction.NAME); // removing 'module' function
        engineBindings.put(CallOSGiServiceFunction.NAME, JSFunctionsImpl.getCallOsgiServiceFunction());
        engineBindings.put(GetOSGiServicesFunction.NAME, JSFunctionsImpl.getGetOsgiServicesFunction());
        engineBindings.put(CallMicroServiceFunction.NAME, JSFunctionsImpl.getCallMicroServiceFunction());
        engineBindings.put(SendErrorFunction.NAME, JSFunctionsImpl.getSendErrorFunction());
        engineBindings.put(SendRedirectFunction.NAME, JSFunctionsImpl.getSendRedirectFunction());
        engineBindings.put(LogFunction.NAME, JSFunctionsImpl.getLogFunction(LoggerFactory.getLogger("JAVASCRIPT")));
        engineBindings.lock();
        // Even though 'NashornScriptEngineFactory.getParameter("THREADING")' returns null, NashornScriptEngine is
        // thread-safe. See http://stackoverflow.com/a/30159424
        this.engine = engine;
        this.engineBindings = engineBindings;
    }

    public Object execute(Object context, API api) {
        try {
            engineBindings.setJSFunctionProvider(new JSFunctionsImpl(api));
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
    public int hashCode() {
        return Objects.hash(scriptPath, engine);
    }

    @Override
    public String toString() {
        return "{\"path\": \"" + scriptPath + "\"}";
    }

    public static class UUFBindings extends SimpleBindings {

        private final ThreadLocal<JSFunctionsImpl> threadLocalFunctionProvider;
        private boolean isLocked;

        public UUFBindings() {
            this.threadLocalFunctionProvider = new ThreadLocal<>();
            this.isLocked = false;
        }

        public void setJSFunctionProvider(JSFunctionsImpl functionProvider) {
            threadLocalFunctionProvider.set(functionProvider);
        }

        public void removeJSFunctionProvider() {
            threadLocalFunctionProvider.remove();
        }

        public void lock() {
            isLocked = true;
        }

        @Override
        public Object get(Object key) {
            if (!(key instanceof String)) {
                return super.get(key);
            }
            switch ((String) key) {
                case CreateSessionFunction.NAME:
                    return threadLocalFunctionProvider.get().getCreateSessionFunction();
                case GetSessionFunction.NAME:
                    return threadLocalFunctionProvider.get().getGetSessionFunction();
                case DestroySessionFunction.NAME:
                    return threadLocalFunctionProvider.get().getDestroySessionFunction();
                case SetAppThemeFunction.NAME:
                    return threadLocalFunctionProvider.get().getSetAppThemeFunction();
                case GetAppThemeFunction.NAME:
                    return threadLocalFunctionProvider.get().getGetAppThemeFunction();
                case SendToClientFunction.NAME:
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
                case CreateSessionFunction.NAME:
                case GetSessionFunction.NAME:
                case DestroySessionFunction.NAME:
                case SetAppThemeFunction.NAME:
                case GetAppThemeFunction.NAME:
                case SendToClientFunction.NAME:
                    return true;
                default:
                    return super.containsKey(key);
            }
        }

        @Override
        public Object put(String name, Object value) {
            if (isLocked) {
                throw new IllegalStateException(
                        "Cannot modify global '" + name + "' variable/function in this context;");
            }
            return super.put(name, value);
        }

        @Override
        public void putAll(Map<? extends String, ? extends Object> toMerge) {
            if (isLocked) {
                throw new IllegalStateException("Cannot modify global variables/functions in this context;");
            }
            super.putAll(toMerge);
        }

        @Override
        public Object remove(Object key) {
            if (isLocked) {
                throw new IllegalStateException(
                        "Cannot modify global '" + key + "' variable/function in this context;");
            }
            return super.remove(key);
        }
    }
}
