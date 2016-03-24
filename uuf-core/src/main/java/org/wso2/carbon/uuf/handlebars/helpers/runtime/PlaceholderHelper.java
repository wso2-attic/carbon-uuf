package org.wso2.carbon.uuf.handlebars.helpers.runtime;

import com.github.jknack.handlebars.Helper;
import com.github.jknack.handlebars.Options;
import org.wso2.carbon.uuf.handlebars.HbsRenderable;
import org.wso2.carbon.uuf.handlebars.MarkedWriter;

import java.io.IOException;

public class PlaceholderHelper implements Helper<String> {
    private static final PlaceholderHelper INSTANCE = new PlaceholderHelper();

    private PlaceholderHelper() {
    }

    public static PlaceholderHelper getInstance() {
        return INSTANCE;
    }

    @Override
    public CharSequence apply(String placeholderName, Options options) throws IOException {
        MarkedWriter writer = options.data(HbsRenderable.WRITER_KEY);
        writer.addMarker(placeholderName);
        return "";
    }
}
