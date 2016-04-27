package org.wso2.carbon.uuf.handlebars.helpers;

import com.github.jknack.handlebars.Context;
import com.github.jknack.handlebars.Handlebars;
import com.github.jknack.handlebars.Options;
import org.wso2.carbon.uuf.core.RequestLookup;
import org.wso2.carbon.uuf.handlebars.helpers.FillPlaceholderHelper;

import java.io.IOException;
import java.util.ArrayList;
import java.util.List;
import java.util.Optional;

import static org.wso2.carbon.uuf.handlebars.HbsRenderable.DATA_KEY_REQUEST_LOOKUP;

public abstract class ResourceHelper extends FillPlaceholderHelper<List<String>> {

    private static final Handlebars.SafeString EMPTY_STRING = new Handlebars.SafeString("");

    public ResourceHelper(String resourcesCategory) {
        super(resourcesCategory);
    }

    @Override
    public CharSequence apply(String relativeUri, Options options) throws IOException {
        if (!relativeUri.startsWith("/")) {
            throw new IllegalArgumentException("Public resource URI should start with '/'.");
        }

        RequestLookup requestLookup = options.data(DATA_KEY_REQUEST_LOOKUP);
        List<String> resources = getValue(options).orElseGet(() -> setValue(options, new ArrayList<>()));
        resources.add(formatResourceUri(requestLookup.getPublicUri() + relativeUri));
        return EMPTY_STRING;
    }

    @Override
    public Optional<String> getPlaceholderValue(Context context) {
        return getValue(context).map(resources -> {
            StringBuilder tmpBuffer = new StringBuilder();
            for (String item : resources) {
                tmpBuffer.append(item);
            }
            return tmpBuffer.toString();
        });
    }

    protected abstract String formatResourceUri(String resourceUri);
}
