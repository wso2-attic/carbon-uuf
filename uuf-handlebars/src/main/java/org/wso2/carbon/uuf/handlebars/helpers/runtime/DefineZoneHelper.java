package org.wso2.carbon.uuf.handlebars.helpers.runtime;

import com.github.jknack.handlebars.Handlebars;
import com.github.jknack.handlebars.Helper;
import com.github.jknack.handlebars.Options;
import org.wso2.carbon.uuf.core.Fragment;
import org.wso2.carbon.uuf.core.Lookup;
import org.wso2.carbon.uuf.handlebars.model.ContextModel;

import java.io.IOException;
import java.util.Collection;
import java.util.Deque;
import java.util.LinkedList;

import static org.wso2.carbon.uuf.handlebars.HbsRenderable.FRAGMENTS_STACK_KEY;
import static org.wso2.carbon.uuf.handlebars.HbsRenderable.LOOKUP_KEY;
import static org.wso2.carbon.uuf.handlebars.HbsRenderable.URI_KEY;

public class DefineZoneHelper implements Helper<String> {

    public static final String HELPER_NAME = "defineZone";

    @Override
    public CharSequence apply(String zoneName, Options options) throws IOException {
        Lookup lookup = options.data(LOOKUP_KEY);
        String uri = options.data(URI_KEY);
        Collection<Fragment> fragments = lookup.lookupBinding(zoneName);
        StringBuilder buffer = new StringBuilder();
        Deque<Fragment> fragmentStack = options.data(FRAGMENTS_STACK_KEY);
        if (fragmentStack == null) {
            fragmentStack = new LinkedList<>();
            options.data(FRAGMENTS_STACK_KEY, fragmentStack);
        }
        for (Fragment fragment : fragments) {
            String content;
            try {
//                fragmentStack.push(fragment);
                content = fragment.render(uri, new ContextModel(options.context), lookup).trim();
                buffer.append(content);
            } finally {
//                fragmentStack.pop();
            }
        }
        return new Handlebars.SafeString(buffer.toString());
    }
}
