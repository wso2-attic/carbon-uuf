package org.wso2.carbon.uuf.handlebars;

import jdk.nashorn.api.scripting.AbstractJSObject;
import jdk.nashorn.api.scripting.NashornScriptEngineFactory;
import org.wso2.carbon.uuf.core.UUFException;

import javax.script.Invocable;
import javax.script.ScriptEngine;
import javax.script.ScriptException;
import java.util.Optional;

@SuppressWarnings("PackageAccessibility")
public class JSExecutable implements Executable {

    private final Optional<String> scriptPath;
    private final Invocable engine;
    private static final NashornScriptEngineFactory factory = new NashornScriptEngineFactory();

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

    public JSExecutable(String scriptSource, Optional<String> scriptPath) {
        this.scriptPath = scriptPath;
        if (scriptPath.isPresent()) {
            // Append script file name for debugging purposes
            scriptSource = scriptSource + "//@ sourceURL=" + getPath();
        }

        try {
            String[] scriptArgs = {"-strict"};
            ScriptEngine engine = factory.getScriptEngine(scriptArgs);
            engine.put("MSSCaller", new MSSCaller());
            engine.eval("var callService = function(method,uri){return JSON.parse(MSSCaller(method,uri))}");
            engine.eval(scriptSource);
            this.engine = (Invocable) engine;
        } catch (ScriptException e) {
            throw new UUFException("error evaluating javascript", e);
        }
    }

    private String getPath() {
        return scriptPath.orElse("\"<in-memory-script>\"");
    }

    public Object execute(Object context) {
        Object rv;
        try {
            rv = engine.invokeFunction("onRequest", context);
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
}
