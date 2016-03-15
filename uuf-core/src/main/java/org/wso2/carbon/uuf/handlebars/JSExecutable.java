package org.wso2.carbon.uuf.handlebars;

import org.wso2.carbon.uuf.core.UUFException;

import javax.script.Invocable;
import javax.script.ScriptEngine;
import javax.script.ScriptEngineManager;
import javax.script.ScriptException;

public class JSExecutable implements Executable {

    private static final ScriptEngineManager engineManager = new ScriptEngineManager();
    private final Invocable engine;
    private final String fileName;

    public JSExecutable(String script, String fileName) {
        this.fileName = fileName;
        try {
            ScriptEngine engine = engineManager.getEngineByName("nashorn");
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
