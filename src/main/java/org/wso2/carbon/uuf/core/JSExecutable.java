package org.wso2.carbon.uuf.core;

import javax.script.Invocable;
import javax.script.ScriptEngine;
import javax.script.ScriptEngineManager;
import javax.script.ScriptException;
import javax.ws.rs.core.Response;

public class JSExecutable implements Executable {

    private static ScriptEngineManager engineManager = new ScriptEngineManager();
    private final Invocable engine;
    private String fileName;

    public JSExecutable(String script, String fileName) {
        this.fileName = fileName;
        try {
            ScriptEngine engine = engineManager.getEngineByName("nashorn");
            engine.eval(script);
            this.engine = (Invocable) engine;
        } catch (ScriptException e) {
            throw new UUFException("error evaluating javascript", Response.Status.INTERNAL_SERVER_ERROR, e);
        }
    }

    @Override
    public Object execute() {
        try {
            return engine.invokeFunction("onRequest");
        } catch (ScriptException e) {
            throw new UUFException(
                    "error while executing " + fileName,
                    Response.Status.INTERNAL_SERVER_ERROR, e);
        } catch (NoSuchMethodException e) {
            throw new UUFException(
                    "method 'onRequest' not defined in " + fileName,
                    Response.Status.INTERNAL_SERVER_ERROR, e);
        }
    }

    @Override
    public String toString() {
        return "{path:'" + fileName + "'}";
    }
}
