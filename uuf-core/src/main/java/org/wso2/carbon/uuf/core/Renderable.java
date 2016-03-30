package org.wso2.carbon.uuf.core;

public interface Renderable {

    String render(String uri, Model model, Lookup lookup);
}
