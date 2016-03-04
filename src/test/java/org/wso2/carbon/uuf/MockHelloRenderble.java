package org.wso2.carbon.uuf;

import org.wso2.carbon.uuf.core.Renderble;

import java.util.Map;

public class MockHelloRenderble implements Renderble {
    private static final String HELLO = "Welcome to the <world> of tomorrow";

    @Override
    public String render(Object o, Map<String, Renderble> zones) {
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
