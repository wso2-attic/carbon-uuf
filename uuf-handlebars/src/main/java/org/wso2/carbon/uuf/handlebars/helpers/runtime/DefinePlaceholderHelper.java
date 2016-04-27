package org.wso2.carbon.uuf.handlebars.helpers.runtime;

import com.github.jknack.handlebars.Helper;
import com.github.jknack.handlebars.Options;
import org.wso2.carbon.uuf.handlebars.PlaceholderWriter;

import java.io.IOException;

import static org.wso2.carbon.uuf.handlebars.HbsRenderable.DATA_KEY_CURRENT_WRITER;

public class DefinePlaceholderHelper implements Helper<String> {

    public static final String HELPER_NAME = "placeholder";

    @Override
    public CharSequence apply(String placeholderName, Options options) throws IOException {
        PlaceholderWriter writer = options.data(DATA_KEY_CURRENT_WRITER);
        writer.addPlaceholder(placeholderName);
        return "";
    }
}
