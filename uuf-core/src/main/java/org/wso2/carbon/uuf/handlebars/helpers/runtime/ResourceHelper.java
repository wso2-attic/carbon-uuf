package org.wso2.carbon.uuf.handlebars.helpers.runtime;

import com.github.jknack.handlebars.Context;
import com.github.jknack.handlebars.Helper;
import com.github.jknack.handlebars.Options;
import org.wso2.carbon.uuf.core.Fragment;

import java.io.IOException;
import java.util.ArrayList;
import java.util.Deque;
import java.util.List;
import java.util.Optional;

import static org.wso2.carbon.uuf.handlebars.HbsRenderable.FRAGMENTS_STACK_KEY;

public abstract class ResourceHelper implements Helper<String> {

    private final String resourcesCategory;

    public ResourceHelper(String resourcesCategory) {
        this.resourcesCategory = this.getClass().getName() + "#" + resourcesCategory;
    }

    @Override
    public CharSequence apply(String relativeUri, Options options) throws IOException {
        if (!relativeUri.startsWith("/")) {
            throw new IllegalArgumentException("A relative URI should start with '/'.");
        }
        List<String> resources = options.data(resourcesCategory);
        if (resources == null) {
            resources = new ArrayList<>();
            options.data(resourcesCategory, resources);
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
        resources.add(format(publicUri));
        return "";
    }

    protected abstract String format(String uri);

    public Optional<String> getResources(Context context) {
        List<String> resourcesList = context.data(this.resourcesCategory);
        if (resourcesList == null || resourcesList.isEmpty()) {
            return Optional.<String>empty();
        }
        StringBuilder tmpBuffer = new StringBuilder();
        for (String item : resourcesList) {
            tmpBuffer.append(item);
        }
        return Optional.of(tmpBuffer.toString());
    }
}
