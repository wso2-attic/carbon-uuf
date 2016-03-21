package org.wso2.carbon.uuf.handlebars.helpers;

import com.github.jknack.handlebars.Helper;
import com.github.jknack.handlebars.Options;

import java.io.IOException;
import java.util.ArrayList;
import java.util.List;

public class ResourceInitHelper implements Helper<String> {

    public static ResourceInitHelper JS_INSTANCE = new ResourceInitHelper("headJs");
    private final String resourceKey;

    private ResourceInitHelper(String resourceType) {
        resourceKey = ResourceInitHelper.class.getName() + "#" + resourceType;
    }

    @Override
    public CharSequence apply(String uri, Options options) throws IOException {
        if (uri.startsWith("/")) {
            throw new IllegalArgumentException("URI fragment relative public URI should start with '/'");
        }
        List<String> resources = options.data(resourceKey);
        if (resources == null) {
            resources = new ArrayList<>();
            options.data(resourceKey, resources);
        }
        resources.add(uri);
        return "";
    }

    public String getResourceKey() {
        return resourceKey;
    }
}
