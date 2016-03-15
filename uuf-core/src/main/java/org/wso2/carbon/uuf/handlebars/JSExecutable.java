package org.wso2.carbon.uuf.handlebars;

import jdk.nashorn.api.scripting.AbstractJSObject;
import jdk.nashorn.api.scripting.NashornScriptEngineFactory;
import org.wso2.carbon.uuf.core.UUFException;

import javax.script.Invocable;
import javax.script.ScriptEngine;
import javax.script.ScriptException;

@SuppressWarnings("PackageAccessibility")
public class JSExecutable implements Executable {

    private final Invocable engine;
    private final String fileName;
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

    public JSExecutable(String script, String fileName) {
        this.fileName = fileName;
        try {
            ScriptEngine engine = factory.getScriptEngine("-strict");
            engine.put("MSSCaller", new MSSCaller());
            engine.eval("var callService = function(method,uri){return JSON.parse(MSSCaller(method,uri))}");
            engine.eval(script + "//@ sourceURL=" + fileName);
            this.engine = (Invocable) engine;
        } catch (ScriptException e) {
            throw new UUFException("error evaluating javascript", e);
        }
    }

    @Override
    public Object execute() {
        try {
            return engine.invokeFunction("onRequest");
        } catch (ScriptException e) {
            throw new UUFException(
                    "error while executing " + fileName,
                    e);
        } catch (NoSuchMethodException e) {
            throw new UUFException(
                    "method 'onRequest' not defined in " + fileName,
                    e);
        }
    }

    @Override
    public String toString() {
        return "{path:'" + fileName + "'}";
    }
}
