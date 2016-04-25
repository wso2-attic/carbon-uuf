package org.wso2.carbon.uuf.handlebars.helpers.init;

import com.github.jknack.handlebars.Helper;
import com.github.jknack.handlebars.Options;
import org.wso2.carbon.uuf.core.exception.UUFException;

import java.io.IOException;

import static org.wso2.carbon.uuf.handlebars.HbsInitRenderable.DATA_KEY_CURRENT_LAYOUT;

public class LayoutHelper implements Helper<String> {

    public static final String HELPER_NAME = "layout";

    @Override
    public CharSequence apply(String layoutName, Options options) throws IOException {
        Object currentLayout = options.data(DATA_KEY_CURRENT_LAYOUT);
        if (currentLayout != null) {
            throw new UUFException("Cannot set layout '" + layoutName + "' to this page because layout '" +
                                           currentLayout + "' is already set.");
        }
        // TODO: 4/22/16 Validate 'layoutName'
        options.data(DATA_KEY_CURRENT_LAYOUT, layoutName);
        return "";
    }
}
