package org.wso2.carbon.uuf.handlebars.helpers.runtime;

import com.github.jknack.handlebars.Helper;
import com.github.jknack.handlebars.Options;
import org.wso2.carbon.uuf.handlebars.PlaceholderWriter;
import org.wso2.carbon.uuf.handlebars.HbsRenderable;

import java.io.IOException;

public class PlaceholderHelper implements Helper<String> {

    public static final String HELPER_NAME = "placeholder";

    @Override
    public CharSequence apply(String placeholderName, Options options) throws IOException {
        PlaceholderWriter writer = options.data(HbsRenderable.DATA_KEY_CURRENT_WRITER);
        writer.addPlaceholder(placeholderName);
        return "";
    }
}
