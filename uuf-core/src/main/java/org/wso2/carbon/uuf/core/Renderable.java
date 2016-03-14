package org.wso2.carbon.uuf.core;

import java.util.Map;

public interface Renderable {

    String render(Map model, Map<String, Fragment> bindings, Map<String, Fragment> fragments) throws UUFException;

}
