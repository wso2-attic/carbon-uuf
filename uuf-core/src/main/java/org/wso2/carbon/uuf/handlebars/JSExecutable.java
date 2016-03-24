package org.wso2.carbon.uuf.handlebars;

import jdk.nashorn.api.scripting.AbstractJSObject;
import jdk.nashorn.api.scripting.NashornScriptEngineFactory;
import org.osgi.framework.*;
import org.osgi.framework.wiring.BundleWiring;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import org.wso2.carbon.uuf.UUFService;
import org.wso2.carbon.uuf.core.BundleCreator;
import org.wso2.carbon.uuf.core.UUFException;
import org.wso2.carbon.uuf.fileio.InMemoryBundleCreator;

import javax.script.Invocable;
import javax.script.ScriptEngine;
import javax.script.ScriptException;
import java.io.IOException;
import java.nio.file.Path;
import java.util.ArrayList;
import java.util.List;
import java.util.Optional;

@SuppressWarnings("PackageAccessibility")
public class JSExecutable implements Executable {

    private static final NashornScriptEngineFactory factory = new NashornScriptEngineFactory();
    private final Optional<String> scriptPath;
    private final Invocable engine;
    private static final Logger log = LoggerFactory.getLogger(JSExecutable.class);

    public JSExecutable(String scriptSource, Optional<String> scriptPath) {
        this.scriptPath = scriptPath;
        if (scriptPath.isPresent()) {
            // Append script file name for debugging purposes
            scriptSource = scriptSource + "//@ sourceURL=" + getPath();
        }

        try {
            ScriptEngine engine = factory.getScriptEngine(new String[] { "-strict" }, getClassLoader());
            engine.put("MSSCaller", new MSSCaller());
            engine.eval("var callService = function(method,uri){return JSON.parse(MSSCaller(method,uri))}");
            engine.eval(scriptSource);
            this.engine = (Invocable) engine;
        } catch (ScriptException e) {
            throw new UUFException("error evaluating javascript", e);
        }
    }

    private ClassLoader getClassLoader(){
        ClassLoader classLoader = this.getClass().getClassLoader();
        if (classLoader instanceof BundleReference) { //check if OSGi classloader
            try {
                List imports = new ArrayList<String>();
                List exports = new ArrayList<String>();
                BundleCreator bundleCreator = new InMemoryBundleCreator();
                Bundle bundle = bundleCreator.createBundle("com.example", "com.example", "1.0.0",
                        Optional.of(imports), Optional.of(exports));
                bundle.start();
                BundleWiring bundleWiring = bundle.adapt(BundleWiring.class);
                //setting specific bundle class loader
                classLoader = bundleWiring.getClassLoader();
            } catch (BundleException e) {
                log.error("Error occurred on installing bundle", e);
            } catch (IOException e) {
                log.error("Error occurred on creating bundle", e);
            }
        }
        return classLoader;
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
