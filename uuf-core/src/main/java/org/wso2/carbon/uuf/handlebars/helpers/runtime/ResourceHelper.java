package org.wso2.carbon.uuf.handlebars.helpers.runtime;

import com.github.jknack.handlebars.Context;
import com.github.jknack.handlebars.Helper;
import com.github.jknack.handlebars.Options;

import java.io.IOException;
import java.util.ArrayList;
import java.util.HashMap;
import java.util.List;
import java.util.Map;

public class ResourceHelper implements Helper<String> {

    private static ResourceHelper HEADER_CSS_INSTANCE = new ResourceHelper("headerCss");
    private static ResourceHelper HEADER_JS_INSTANCE = new ResourceHelper("headerJs");
    private static ResourceHelper FOOTER_JS_INSTANCE = new ResourceHelper("footerJs");
    private final String resourceKey;

    private ResourceHelper(String resourceType) {
        resourceKey = ResourceHelper.class.getName() + "#" + resourceType;
    }

    public static ResourceHelper getHeaderCssInstance() {
        return HEADER_CSS_INSTANCE;
    }

    public static ResourceHelper getHeaderJsInstance() {
        return HEADER_JS_INSTANCE;
    }

    public static ResourceHelper getFooterJsInstance() {
        return FOOTER_JS_INSTANCE;
    }

    @Override
    public CharSequence apply(String uri, Options options) throws IOException {
        if (!uri.startsWith("/")) {
            throw new IllegalArgumentException("A relative public URI should start with '/'.");
        }
        List<String> resources = options.data(resourceKey);
        if (resources == null) {
            resources = new ArrayList<>();
            options.data(resourceKey, resources);
        }
        resources.add(uri);
        return "";
    }

    public static Map<String, String> getAllResources(Context context) {
        String headerJsKey = ResourceHelper.class.getName() + "#headerJs";
        List<String> headJsList = context.data(headerJsKey);
        Map<String, String> rv = new HashMap<>();
        rv.put("headerJs", ((headJsList == null || headJsList.isEmpty()) ? "" : headJsList.toString()));
        return rv;
    }

    private List<String> getResources(Context context) {
        List<String> resourcesList = context.data(this.resourceKey);
        return (resourcesList == null) ? new ArrayList<>(0) : resourcesList;
    }
}
