package org.wso2.carbon.uuf.handlebars.helpers.runtime;

import com.github.jknack.handlebars.Options;
import org.wso2.carbon.uuf.core.Fragment;

import java.io.IOException;
import java.util.ArrayList;
import java.util.Deque;
import java.util.List;

import static org.wso2.carbon.uuf.handlebars.HbsRenderable.FRAGMENTS_STACK_KEY;

public abstract class ResourceHelper extends FillPlaceholderHelper {

    public ResourceHelper(String resourcesCategory) {
        super(resourcesCategory);
    }

    @Override
    public CharSequence apply(String relativeUri, Options options) throws IOException {
        if (!relativeUri.startsWith("/")) {
            throw new IllegalArgumentException("A relative URI should start with '/'.");
        }
        List<String> resources = options.data(placeholderName);
        if (resources == null) {
            resources = new ArrayList<>();
            options.data(placeholderName, resources);
        }
        Deque<Fragment> fragmentStack = options.data(FRAGMENTS_STACK_KEY);
        String publicUri;
        if ((fragmentStack == null) || fragmentStack.isEmpty()) {
            // this resource is adding in a page or layout
            //TODO get component's public URI
            publicUri = "/public/component-name/base" + relativeUri;
        } else {
            publicUri = fragmentStack.peekLast().getResourceUriPrefix() + relativeUri;
        }
        resources.add(formatValue(publicUri));
        return "";
    }

}
