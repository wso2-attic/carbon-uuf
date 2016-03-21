package org.wso2.carbon.uuf.handlebars.helpers;

import com.github.jknack.handlebars.Helper;
import com.github.jknack.handlebars.Options;
import org.wso2.carbon.uuf.core.UUFException;
import org.wso2.carbon.uuf.handlebars.HbsPageRenderable;

import java.io.IOException;

public class LayoutInitHelper implements Helper<String> {

    public static final String LAYOUT_KEY = HbsPageRenderable.class.getName() + "#layout";
    public static final LayoutInitHelper INSTANCE = new LayoutInitHelper();

    private LayoutInitHelper() {
    }

    @Override
    public CharSequence apply(String layoutName, Options options) throws IOException {
        Object originalLayout = options.data(LAYOUT_KEY);
        if (originalLayout != null) {
            throw new UUFException(
                    "multiple layout '" + layoutName + "','" + originalLayout + "'  defined");
        }
        options.data(LAYOUT_KEY, layoutName);
        return "";
    }
}
