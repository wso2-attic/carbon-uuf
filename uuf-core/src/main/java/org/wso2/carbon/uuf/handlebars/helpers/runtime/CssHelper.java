package org.wso2.carbon.uuf.handlebars.helpers.runtime;

public class CssHelper extends ResourceHelper {

    public static final String HELPER_NAME = "css";

    public CssHelper() {
        super(HELPER_NAME);
    }

    @Override
    protected String formatResourceUri(String resourceUri) {
        return "<link href=\"" + resourceUri + "\" rel=\"stylesheet\" type=\"text/css\" />";
    }
}
