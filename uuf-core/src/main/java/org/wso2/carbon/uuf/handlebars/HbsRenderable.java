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
import java.util.Collections;
import java.util.Map;
import java.util.Optional;

public class HbsRenderable implements Renderable {
    private final TemplateSource template;
    private final Optional<Path> templatePath;
    private final Optional<JSExecutable> script;

    public HbsRenderable(String templateSource) {
        this(templateSource, Optional.empty(), Optional.empty(), Optional.empty());
    }

    public HbsRenderable(String templateSource, Path templatePath) {
        this(templateSource, Optional.of(templatePath), Optional.empty(), Optional.empty());
    }

    public HbsRenderable(String templateSource, Path templatePath, String scriptSource, Path scriptPath) {
        this(templateSource, Optional.of(templatePath), Optional.of(scriptSource), Optional.of(scriptPath));
    }

    public HbsRenderable(String templateSource, Path templatePath, Optional<JSExecutable> script) {
        this.templatePath = Optional.of(templatePath);
        this.template = new StringTemplateSource(getPath(), templateSource);
        this.script = script;
    }

    private HbsRenderable(String templateSource, Optional<Path> templatePath, Optional<String> scriptSource,
                          Optional<Path> scriptPath) {
        this.templatePath = templatePath;
        this.template = new StringTemplateSource(getPath(), templateSource);
        this.script = scriptSource.map((s) -> new JSExecutable(s, scriptPath));
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
        Object jsModel = script.map(e -> e.execute(model)).orElse(Collections.EMPTY_MAP);
        Context context = Context.newContext(jsModel);
        //TODO: detect uncombined scenarios
        if (model instanceof Context) {
            Context parentContext = (Context) model;
            if (parentContext.model() instanceof Map) {
                //noinspection unchecked
                context.combine((Map) parentContext.model());
            }
        }
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
        return "{\"path\": \"" + getPath() + "\"}";
    }

    private String getPath() {
        return templatePath.map(Path::toString).orElse("\"<inline-template>\"");
    }
}
