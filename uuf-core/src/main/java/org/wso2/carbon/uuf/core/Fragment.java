package org.wso2.carbon.uuf.core;


import java.util.Map;
import java.util.Optional;

public class Fragment implements Renderable {

    private String name;
    private final Renderable template;
    private final Optional<Executable> script;

    public Fragment(String name, Renderable template, Optional<Executable> script) {
        this.name = name;
        this.template = template;
        this.script = script;
    }

    @Override
    public String render(Object o, Map<String, Renderable> zones, Map<String, Renderable> fragments) {
        Object templateInput;
        if (script.isPresent()) {
            templateInput = script.get().execute();
        } else {
            templateInput = o;
        }
        return template.render(templateInput, zones, fragments);
    }

    public String getName() {
        return name;
    }
}
