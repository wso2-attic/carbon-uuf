package org.wso2.carbon.uuf.handlebars.helpers.runtime;

public class JsHelper extends ResourceHelper {

    public JsHelper(String resourceType) {
        super(resourceType);
    }

    @Override
    protected String format(String uri) {
        return uri;
    }
}
