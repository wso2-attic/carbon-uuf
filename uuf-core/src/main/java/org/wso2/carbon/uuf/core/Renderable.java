package org.wso2.carbon.uuf.core;

import org.wso2.carbon.uuf.model.Model;

public interface Renderable {

    String render(Model model, StaticLookup staticLookup, RequestLookup requestLookup, API api);
}
