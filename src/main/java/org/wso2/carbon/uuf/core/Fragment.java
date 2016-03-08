package org.wso2.carbon.uuf.core;


import javax.annotation.Nullable;
import java.util.Map;

public class Fragment implements Renderble {

    private final Renderble template;
    @Nullable
    private Executable script;

    public Fragment(Renderble template, @Nullable Executable script) {
        this.template = template;
        this.script = script;
    }

    @Override
    public String render(Object o, Map<String, Renderble> zones, Map<String, Renderble> fragments) {
        Object templateInput;
        if (script != null) {
            templateInput = script.execute();
        } else {
            templateInput = o;
        }
        return template.render(templateInput, zones, fragments);
    }
}
