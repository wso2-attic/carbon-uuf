package org.wso2.carbon.uuf.handlebars;

import com.github.jknack.handlebars.Context;
import com.github.jknack.handlebars.Template;
import com.github.jknack.handlebars.io.StringTemplateSource;
import com.github.jknack.handlebars.io.TemplateSource;
import com.google.common.collect.Multimap;
import org.wso2.carbon.uuf.core.Fragment;
import org.wso2.carbon.uuf.core.Renderable;
import org.wso2.carbon.uuf.core.UUFException;
import org.wso2.carbon.uuf.handlebars.util.RuntimeHandlebarsUtil;

import java.io.IOException;
import java.nio.file.Path;
import java.util.Map;
import java.util.Optional;

public class HbsRenderable implements Renderable {
    private final TemplateSource template;
    private final Optional<Path> templatePath;
    private final Optional<JSExecutable> script;

    public HbsRenderable(String templateSource) {
        this(templateSource, Optional.<Path>empty(), Optional.<String>empty(), Optional.<Path>empty());
    }

    public HbsRenderable(String templateSource, Path templatePath) {
        this(templateSource, Optional.of(templatePath), Optional.<String>empty(), Optional.<Path>empty());
    }

    public HbsRenderable(String templateSource, Path templatePath, String scriptSource, Path scriptPath) {
        this(templateSource, Optional.of(templatePath), Optional.of(scriptSource), Optional.of(scriptPath));
    }

    private HbsRenderable(String templateSource, Optional<Path> templatePath, Optional<String> scriptSource,
                          Optional<Path> scriptPath) {
        this.templatePath = templatePath;
        String fileName = templatePath.isPresent() ? templatePath.get().toString() : "";
        this.template = new StringTemplateSource(fileName, templateSource);
        this.script = scriptSource.isPresent() ? Optional.of(new JSExecutable(scriptSource.get(), scriptPath)) :
                Optional.<JSExecutable>empty();
    }

    public HbsRenderable(String templateSource, Path templatePath, Optional<JSExecutable> script) {
        this.templatePath = Optional.of(templatePath);
        String fileName = this.templatePath.isPresent() ? this.templatePath.get().toString() : "";
        this.template = new StringTemplateSource(fileName, templateSource);
        this.script = script;
    }

    public Optional<JSExecutable> getScript() {
        return script;
    }

    public TemplateSource getTemplate() {
        return template;
    }

    @Override
    public String render(Object model, Multimap<String, Renderable> bindings,
                         Map<String, Fragment> fragments) {
        Object jsModel = script.isPresent() ? script.get().execute(model) : new Object();
        Context context = Context.newContext(jsModel);
        if (model instanceof Context) {
            Context parentContext = (Context) model;
            context.combine((Map) parentContext.model());

        }
        //.combine( model);
        RuntimeHandlebarsUtil.setBindings(context, bindings);
        RuntimeHandlebarsUtil.setFragments(context, fragments);
        try {
            Template compiledTemplate = RuntimeHandlebarsUtil.compile(template);
            return compiledTemplate.apply(context);
        } catch (IOException e) {
            throw new UUFException("Handlebars rendering failed", e);
        }
    }

    @Override
    public String toString() {
        return "{\"path\": \"" + (templatePath.isPresent() ? templatePath.get().toString() : "") + "\"}";
    }
}
