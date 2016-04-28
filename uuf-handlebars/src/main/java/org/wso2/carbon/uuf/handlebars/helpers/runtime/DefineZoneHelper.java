package org.wso2.carbon.uuf.handlebars.helpers.runtime;

import com.github.jknack.handlebars.Handlebars;
import com.github.jknack.handlebars.Helper;
import com.github.jknack.handlebars.Options;
import org.wso2.carbon.uuf.core.API;
import org.wso2.carbon.uuf.core.ComponentLookup;
import org.wso2.carbon.uuf.core.Fragment;
import org.wso2.carbon.uuf.core.RequestLookup;
import org.wso2.carbon.uuf.handlebars.model.ContextModel;

import java.io.IOException;
import java.util.Set;

import static org.wso2.carbon.uuf.handlebars.HbsRenderable.DATA_KEY_API;
import static org.wso2.carbon.uuf.handlebars.HbsRenderable.DATA_KEY_LOOKUP;
import static org.wso2.carbon.uuf.handlebars.HbsRenderable.DATA_KEY_REQUEST_LOOKUP;

public class DefineZoneHelper implements Helper<String> {

    public static final String HELPER_NAME = "defineZone";

    @Override
    public CharSequence apply(String zoneName, Options options) throws IOException {
        ComponentLookup lookup = options.data(DATA_KEY_LOOKUP);
        RequestLookup requestLookup = options.data(DATA_KEY_REQUEST_LOOKUP);
        StringBuilder buffer = new StringBuilder();

        Set<Fragment> bindings = lookup.getBindings(zoneName);
        if(!bindings.isEmpty()){
            API api = options.data(DATA_KEY_API);
            for (Fragment fragment : bindings) {
                buffer.append(fragment.render(new ContextModel(options.context), lookup, requestLookup, api));
            }
        }

        requestLookup.getZoneContent(zoneName).ifPresent(buffer::append);
        return new Handlebars.SafeString(buffer.toString());
    }
}
