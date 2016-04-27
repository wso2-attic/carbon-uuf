package org.wso2.carbon.uuf.handlebars.helpers.runtime;

import com.github.jknack.handlebars.Options;
import org.wso2.carbon.uuf.core.RequestLookup;
import org.wso2.carbon.uuf.handlebars.helpers.FillPlaceholderHelper;

import java.io.IOException;

import static org.wso2.carbon.uuf.handlebars.HbsRenderable.DATA_KEY_REQUEST_LOOKUP;

public class HeaderOtherHelper extends FillPlaceholderHelper {

    public static final String HELPER_NAME = "headerOther";

    protected HeaderOtherHelper() {
        super(HELPER_NAME);
    }

    @Override
    public CharSequence apply(String s, Options options) throws IOException {
        RequestLookup requestLookup = options.data(DATA_KEY_REQUEST_LOOKUP);
        requestLookup.addToPlaceholder(placeholderName, options.fn().toString());
        return "";
    }
}
