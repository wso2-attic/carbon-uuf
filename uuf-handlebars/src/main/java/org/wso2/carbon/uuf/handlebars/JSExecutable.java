package org.wso2.carbon.uuf.handlebars;

import jdk.nashorn.api.scripting.NashornScriptEngine;
import jdk.nashorn.api.scripting.NashornScriptEngineFactory;
import org.wso2.carbon.uuf.core.API;
import org.wso2.carbon.uuf.core.exception.UUFException;

import javax.script.ScriptException;
import java.util.Map;
import java.util.Optional;

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
        engine.put("callOSGiService", (FunctionCallOSGiService) api::callOSGiService);
        engine.put("getOSGiServices", (FunctionGetOSGiServices) api::getOSGiServices);
        engine.put("callMicroService", (FunctionCallMicroService) api::callMicroService);
        engine.put("createSession", (FunctionCreateSession) api::createSession);
        engine.put("sendError", (FunctionSendError) api::sendError);
        engine.put("sendRedirect", (FunctionSendRedirect) api::sendRedirect);
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
