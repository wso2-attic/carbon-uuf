package org.wso2.carbon.uuf.handlebars.helpers.runtime;

import com.github.jknack.handlebars.Handlebars;
import com.github.jknack.handlebars.Helper;
import com.github.jknack.handlebars.Options;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import org.wso2.carbon.uuf.core.Fragment;
import org.wso2.carbon.uuf.core.Lookup;
import org.wso2.carbon.uuf.core.MapModel;
import org.wso2.carbon.uuf.core.Model;
import org.wso2.carbon.uuf.handlebars.ContextModel;

import java.io.IOException;
import java.util.Deque;
import java.util.LinkedList;
import java.util.Map;

import static org.wso2.carbon.uuf.handlebars.HbsRenderable.FRAGMENTS_STACK_KEY;
import static org.wso2.carbon.uuf.handlebars.HbsRenderable.LOOKUP_KEY;
import static org.wso2.carbon.uuf.handlebars.HbsRenderable.URI_KEY;

public class IncludeFragmentHelper implements Helper<String> {
    public static final String HELPER_NAME = "includeFragment";
    private static final Logger log = LoggerFactory.getLogger(IncludeFragmentHelper.class);

    @Override
    public CharSequence apply(String fragmentName, Options options) throws IOException {
        Lookup lookup = options.data(LOOKUP_KEY);
        String uri = options.data(URI_KEY);
        Fragment fragment = lookup.lookupFragment(fragmentName);

        Map<String, Object> fragmentArgs = options.hash;
        Model fragmentContext;
        if (fragmentArgs.isEmpty()) {
            fragmentContext = new ContextModel(options.context);
        } else {
            fragmentContext = new MapModel(fragmentArgs);
        }

        if (log.isDebugEnabled()) {
            log.debug("Fragment " + fragment + " is called from '" + options.fn.text() + "'.");
        }
        Deque<Fragment> fragmentStack = options.data(FRAGMENTS_STACK_KEY);
        if (fragmentStack == null) {
            fragmentStack = new LinkedList<>();
            options.data(FRAGMENTS_STACK_KEY, fragmentStack);
        }
        fragmentStack.push(fragment);
        String content = fragment.render(uri, fragmentContext, lookup).trim();
        fragmentStack.pop();
        return new Handlebars.SafeString(content);
    }
}
