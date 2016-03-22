package org.wso2.carbon.uuf.handlebars.helpers.runtime;

import com.github.jknack.handlebars.Handlebars;
import com.github.jknack.handlebars.Helper;
import com.github.jknack.handlebars.Options;
import com.google.common.collect.Multimap;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import org.wso2.carbon.uuf.core.Fragment;
import org.wso2.carbon.uuf.core.Renderable;
import org.wso2.carbon.uuf.core.UUFException;

import java.io.IOException;
import java.util.Map;

import static org.wso2.carbon.uuf.handlebars.HbsRenderable.BINDING_KEY;
import static org.wso2.carbon.uuf.handlebars.HbsRenderable.FRAGMENT_KEY;

public class IncludeFragmentHelper implements Helper<String> {
    private static final Logger log = LoggerFactory.getLogger(IncludeFragmentHelper.class);
    public static final IncludeFragmentHelper INSTANCE = new IncludeFragmentHelper();

    @Override
    public CharSequence apply(String fragmentName, Options options) throws IOException {
        //TODO: remove duplicate, defineZone
        Multimap<String, Renderable> bindings = options.data(BINDING_KEY);
        Map<String, Fragment> fragments = options.data(FRAGMENT_KEY);
        Fragment fragment = fragments.get(fragmentName);
        if (fragment != null) {
            Map<String, Object> fragmentArgs = options.hash;
            Object fragmentContext;
            if (fragmentArgs.isEmpty()) {
                fragmentContext = options.context;
            } else {
                fragmentContext = fragmentArgs;
            }

            if (log.isDebugEnabled()) {
                log.debug("Fragment " + fragment + " is called from " + options.fn);
            }
            String content = fragment.render(fragmentContext, bindings, fragments).trim();
            return new Handlebars.SafeString(content);
        }
        throw new UUFException("fragment '" + fragmentName + "' not available");
    }
}
