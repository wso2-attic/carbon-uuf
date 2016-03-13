package org.wso2.carbon.uuf;

import org.wso2.carbon.uuf.core.Renderable;

import java.util.Map;

public class MockHelloRenderable implements Renderable {
    private static final String HELLO = "Welcome to the <world> of tomorrow";

    @Override
    public String render(Object o, Map<String, Renderable> zones, Map<String, Renderable> fragments) {
        if (o instanceof Map) {
            String name = (String) ((Map) o).get("name");
            if (name != null) {
                return HELLO + ", " + name;
            } else {
                return HELLO + " !";
            }
        } else {
            return HELLO + ", " + o.toString();
        }
    }

}
