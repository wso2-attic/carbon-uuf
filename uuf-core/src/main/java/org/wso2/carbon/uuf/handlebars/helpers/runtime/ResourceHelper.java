package org.wso2.carbon.uuf.handlebars.helpers.runtime;

import com.github.jknack.handlebars.Context;
import com.github.jknack.handlebars.Helper;
import com.github.jknack.handlebars.Options;

import java.io.IOException;
import java.util.ArrayList;
import java.util.List;
import java.util.Optional;

public abstract class ResourceHelper implements Helper<String> {

    private final String resourcesCategory;

    public ResourceHelper(String resourcesCategory) {
        this.resourcesCategory = this.getClass().getName() + "#" + resourcesCategory;
    }

    @Override
    public CharSequence apply(String uri, Options options) throws IOException {
        if (!uri.startsWith("/")) {
            throw new IllegalArgumentException("A relative public URI should start with '/'.");
        }
        List<String> resources = options.data(resourcesCategory);
        if (resources == null) {
            resources = new ArrayList<>();
            options.data(resourcesCategory, resources);
        }
        resources.add(format(uri));
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
