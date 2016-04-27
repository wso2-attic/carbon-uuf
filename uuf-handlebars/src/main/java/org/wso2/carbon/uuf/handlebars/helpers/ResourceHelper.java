package org.wso2.carbon.uuf.handlebars.helpers;

import com.github.jknack.handlebars.Options;
import org.wso2.carbon.uuf.core.RequestLookup;

import java.io.IOException;

import static org.wso2.carbon.uuf.handlebars.HbsRenderable.DATA_KEY_REQUEST_LOOKUP;

public abstract class ResourceHelper extends FillPlaceholderHelper {

    public ResourceHelper(String resourcesCategory) {
        super(resourcesCategory);
    }

    @Override
    public CharSequence apply(String relativeUri, Options options) throws IOException {
        if (!relativeUri.startsWith("/")) {
            throw new IllegalArgumentException("Public resource URI should start with '/'.");
        }

        RequestLookup requestLookup = options.data(DATA_KEY_REQUEST_LOOKUP);
        requestLookup.putToZone(placeholderName, formatResourceUri(requestLookup.getPublicUri() + relativeUri));
        return "";
    }

    protected abstract String formatResourceUri(String resourceUri);
}
