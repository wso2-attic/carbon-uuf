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
    private final Optional<Executable> executable;
    private final Template compiledTemplate;

    public HbsRenderable(String templateSource) {
        this(templateSource, Optional.<Path>empty(), Optional.<Executable>empty());
    }

    public HbsRenderable(String templateSource, Path templatePath) {
        this(templateSource, Optional.of(templatePath), Optional.<Executable>empty());
    }

    public HbsRenderable(String templateSource, Executable executable) {
        this(templateSource, Optional.<Path>empty(), Optional.of(executable));
    }

    public HbsRenderable(String templateSource, Path templatePath, Executable executable) {
        this(templateSource, Optional.of(templatePath), Optional.of(executable));
    }

    private HbsRenderable(String templateSource, Optional<Path> templatePath, Optional<Executable> executable) {
        this.templatePath = templatePath;
        this.template = new StringTemplateSource(getPath(), templateSource);
        this.executable = executable;
        this.compiledTemplate = RuntimeHandlebarsUtil.compile(template);
    }

    public Optional<Executable> getScript() {
        return executable;
    }

    public TemplateSource getTemplate() {
        return template;
    }

    private String getPath() {
        return templatePath.map(Path::toString).orElse("\"<inline-template>\"");
    }

    @Override
    public String render(Object model, Multimap<String, Renderable> bindings, Map<String, Fragment> fragments) {
        Context context = objectToContext(model);
        if (executable.isPresent()) {
            Object jsModel = executable.get().execute(Collections.EMPTY_MAP);
            if (jsModel instanceof Map) {
                //noinspection unchecked
                context.combine((Map<String, ?>) jsModel);
            } else {
                throw new UnsupportedOperationException();
            }
        }
        RuntimeHandlebarsUtil.setBindings(context, bindings);
        RuntimeHandlebarsUtil.setFragments(context, fragments);
        try {
            return compiledTemplate.apply(context);
        } catch (IOException e) {
            throw new UUFException("Handlebars rendering failed", e);
        }
    }

    private Context objectToContext(Object candidateContext) {
        if (candidateContext instanceof Context) {
            return (Context) candidateContext;
        } else {
            return Context.newContext(candidateContext);
        }
    }

    @Override
    public String toString() {
        return "{\"path\": \"" + getPath() + "\"}";
    }
}
