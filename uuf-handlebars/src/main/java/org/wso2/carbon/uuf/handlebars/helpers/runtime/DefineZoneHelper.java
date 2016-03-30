package org.wso2.carbon.uuf.handlebars.helpers.runtime;

import com.github.jknack.handlebars.Handlebars;
import com.github.jknack.handlebars.Helper;
import com.github.jknack.handlebars.Options;
import org.wso2.carbon.uuf.core.Lookup;
import org.wso2.carbon.uuf.core.Renderable;
import org.wso2.carbon.uuf.handlebars.ContextModel;

import java.io.IOException;
import java.util.Collection;
import java.util.Map;

import static org.wso2.carbon.uuf.handlebars.HbsRenderable.LOOKUP_KEY;
import static org.wso2.carbon.uuf.handlebars.HbsRenderable.URI_KEY;

public class DefineZoneHelper implements Helper<String> {

    public static final String HELPER_NAME = "defineZone";

    @Override
    public CharSequence apply(String zoneName, Options options) throws IOException {
        Lookup lookup = options.data(LOOKUP_KEY);
        String uri = options.data(URI_KEY);
        Collection<Renderable> renderables = lookup.lookupBinding(zoneName);
        StringBuilder buffer = new StringBuilder();
        for (Renderable renderable : renderables) {
            String content = renderable.render(uri, new ContextModel(options.context), lookup).trim();
            buffer.append(content);
        }
        return new Handlebars.SafeString(buffer.toString());
    }
}
