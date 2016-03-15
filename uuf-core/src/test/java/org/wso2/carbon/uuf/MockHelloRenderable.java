package org.wso2.carbon.uuf;

import com.google.common.collect.Multimap;
import org.wso2.carbon.uuf.core.Fragment;
import org.wso2.carbon.uuf.core.Renderable;

import java.util.Map;

public class MockHelloRenderable implements Renderable {
    private static final String HELLO = "Welcome to the <world> of tomorrow";

    @Override
    public String render(Object model, Multimap<String, Renderable> bindings, Map<String, Fragment> fragments) {
        if (model instanceof Map) {
            String name = (String) ((Map) model).get("name");
            if (name != null) {
                return HELLO + ", " + name;
            } else {
                return HELLO + " !";
            }
        } else {
            return HELLO + ", " + model.toString();
        }
    }
}
