package org.wso2.carbon.uuf.handlebars.helpers.runtime;

public class CssHelper extends ResourceHelper {

    public CssHelper(String resourceType) {
        super(resourceType);
    }

    @Override
    protected String format(String uri) {
        return "<link href=\"" + uri + "\" rel=\"stylesheet\" type=\"text/css\" />";
    }
}
