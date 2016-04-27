package org.wso2.carbon.uuf.handlebars.helpers.runtime;

import com.github.jknack.handlebars.Helper;
import com.github.jknack.handlebars.Options;
import org.wso2.carbon.uuf.core.RequestLookup;

import java.io.IOException;

import static org.wso2.carbon.uuf.handlebars.HbsRenderable.DATA_KEY_REQUEST_LOOKUP;

public class FillZoneHelper implements Helper<String> {

    public static final String HELPER_NAME = "fillZone";

    @Override
    public CharSequence apply(String zoneName, Options options) throws IOException {
        if (zoneName.isEmpty()) {
            throw new IllegalArgumentException("A zone name cannot be empty.");
        }
        RequestLookup requestLookup = options.data(DATA_KEY_REQUEST_LOOKUP);
        requestLookup.putToZone(zoneName, options.fn().toString());
        return "";
    }
}
