package org.wso2.carbon.uuf.handlebars.helpers.runtime;

public class CssHelper extends ResourceHelper {

    public static final String HELPER_NAME = "css";

    public CssHelper() {
        super(HELPER_NAME);
    }

    @Override
    protected String format(String uri) {
        return "<link href=\"" + uri + "\" rel=\"stylesheet\" type=\"text/css\" />";
    }
}
