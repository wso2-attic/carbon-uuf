package org.wso2.carbon.uuf.handlebars.helpers.runtime;

import com.github.jknack.handlebars.Context;
import com.github.jknack.handlebars.Handlebars;
import com.github.jknack.handlebars.Options;
import org.wso2.carbon.uuf.core.Fragment;

import java.io.IOException;
import java.util.ArrayList;
import java.util.Deque;
import java.util.List;
import java.util.Optional;

import static org.wso2.carbon.uuf.handlebars.HbsRenderable.COMPONENT_NAME_KEY;
import static org.wso2.carbon.uuf.handlebars.HbsRenderable.FRAGMENTS_STACK_KEY;
import static org.wso2.carbon.uuf.handlebars.HbsRenderable.URI_KEY;

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
        List<String> resources = getValue(options).orElseGet(() -> setValue(options, new ArrayList<>()));
        Deque<Fragment> fragmentStack = options.data(FRAGMENTS_STACK_KEY);
        String uriUpToContext = options.data(URI_KEY);
        String component = options.data(COMPONENT_NAME_KEY);
        String publicUri;
        if ((fragmentStack == null) || fragmentStack.isEmpty()) {
            // this resource is adding in a page or layout
            //TODO get component's public URI
            publicUri = uriUpToContext + "/public" + component + "/base" + relativeUri;
        } else {
            publicUri = uriUpToContext + "/public" + fragmentStack.peekLast().getPublicContext() + relativeUri;
        }
        resources.add(formatResourceUri(publicUri));
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
