package org.wso2.carbon.uuf.handlebars.helpers.runtime;

public class JsHelper extends ResourceHelper {

    public static final String HELPER_NAME_HEADER = "headerJs";
    public static final String HELPER_NAME_FOOTER = "footerJs";

    public JsHelper(String helperName) {
        super(helperName);
    }

    @Override
    protected String formatResourceUri(String resourceUri) {
        return "<script src=\"" + resourceUri + "\"></script>";
    }
}
