package org.wso2.carbon.uuf.handlebars;

import jdk.nashorn.api.scripting.AbstractJSObject;
import jdk.nashorn.api.scripting.NashornScriptEngineFactory;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import org.wso2.carbon.uuf.core.API;
import org.wso2.carbon.uuf.core.exception.UUFException;

import javax.script.Invocable;
import javax.script.ScriptEngine;
import javax.script.ScriptException;
import java.util.Optional;

@SuppressWarnings("PackageAccessibility")
public class JSExecutable implements Executable {

    private static final NashornScriptEngineFactory factory = new NashornScriptEngineFactory();
    private final Optional<String> scriptPath;
    private final ScriptEngine engine;
    private static final Logger log = LoggerFactory.getLogger(JSExecutable.class);

    public JSExecutable(String scriptSource, ClassLoader componentClassLoader, Optional<String> scriptPath) {
        this.scriptPath = scriptPath;
        if (scriptPath.isPresent()) {
            // Append script file name for debugging purposes
            scriptSource = scriptSource + "//@ sourceURL=" + getPath();
        }

        try {
            ScriptEngine engine = factory.getScriptEngine(new String[] { "-strict" }, componentClassLoader);
            engine.eval("var callOSGiService = function(className, methodName, args){return API.callOSGiService(className, methodName, args)}");
            engine.eval(scriptSource);
            this.engine = engine;
        } catch (ScriptException e) {
            throw new UUFException("error evaluating javascript", e);
        }
    }

    private String getPath() {
        return scriptPath.orElse("\"<in-memory-script>\"");
    }

    public Object execute(Object context, API api) {
        Object rv;
        try {
            engine.put("API", api);
            rv = ((Invocable)engine).invokeFunction("onRequest", context);
        } catch (ScriptException e) {
            throw new UUFException("error while executing script " + getPath(), e);
        } catch (NoSuchMethodException e) {
            throw new UUFException("method 'onRequest' not defined in the script " + getPath(), e);
        }
        return rv;
    }

    @Override
    public String toString() {
        return "{path:'" + getPath() + "'}";
    }

    @Deprecated
    private class MSSCaller extends AbstractJSObject {
        @Override
        public Object call(Object jsThis, Object... args) {
            return "{}";
        }

        @Override
        public boolean isFunction() {
            return true;
        }
    }
}
