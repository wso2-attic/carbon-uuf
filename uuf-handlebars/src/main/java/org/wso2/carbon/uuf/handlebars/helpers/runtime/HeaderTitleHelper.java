package org.wso2.carbon.uuf.handlebars.helpers.runtime;

import com.github.jknack.handlebars.Options;
import org.wso2.carbon.uuf.core.RequestLookup;
import org.wso2.carbon.uuf.handlebars.helpers.FillPlaceholderHelper;

import java.io.IOException;
import java.util.Optional;

import static org.wso2.carbon.uuf.handlebars.HbsRenderable.DATA_KEY_REQUEST_LOOKUP;

public class HeaderTitleHelper extends FillPlaceholderHelper {
    public static final String HELPER_NAME = "headerTitle";

    public HeaderTitleHelper() {
        super(HELPER_NAME);
    }

    public CharSequence apply(String title, Options options) throws IOException {
        RequestLookup requestLookup = options.data(DATA_KEY_REQUEST_LOOKUP);
        Optional<String> currentTitle = requestLookup.getPlaceholderContent(placeholderName);
        if (currentTitle.isPresent()) {
            throw new IllegalStateException(
                    "Cannot set header title. It is already set to '" + currentTitle.get() + "'.");
        }
        requestLookup.addToPlaceholder(HELPER_NAME, title);
        return "";
    }
}
