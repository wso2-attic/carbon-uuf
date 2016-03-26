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
import java.util.Deque;
import java.util.LinkedList;
import java.util.Map;

import static org.wso2.carbon.uuf.handlebars.HbsRenderable.BINDING_KEY;
import static org.wso2.carbon.uuf.handlebars.HbsRenderable.FRAGMENTS_STACK_KEY;
import static org.wso2.carbon.uuf.handlebars.HbsRenderable.FRAGMENT_KEY;

public class IncludeFragmentHelper implements Helper<String> {
    private static final Logger log = LoggerFactory.getLogger(IncludeFragmentHelper.class);

    @Override
    public CharSequence apply(String fragmentName, Options options) throws IOException {
        //TODO: remove duplicate, defineZone
        Multimap<String, Renderable> bindings = options.data(BINDING_KEY);
        Map<String, Fragment> fragments = options.data(FRAGMENT_KEY);
        Fragment fragment = fragments.get(fragmentName);
        if (fragment == null) {
            throw new UUFException("Fragment '" + fragmentName + "' does not exists.");
        }

        Map<String, Object> fragmentArgs = options.hash;
        Object fragmentContext;
        if (fragmentArgs.isEmpty()) {
            fragmentContext = options.context;
        } else {
            fragmentContext = fragmentArgs;
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
        String content = fragment.render(fragmentContext, bindings, fragments).trim();
        fragmentStack.pop();
        return new Handlebars.SafeString(content);
    }
}
