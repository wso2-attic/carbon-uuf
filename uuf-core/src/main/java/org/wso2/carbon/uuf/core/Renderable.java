package org.wso2.carbon.uuf.core;

import com.google.common.collect.Multimap;

import java.util.Map;

public interface Renderable {

    String render(Map model, Multimap<String, Renderable> bindings, Map<String, Fragment> fragments)
            throws UUFException;
}
