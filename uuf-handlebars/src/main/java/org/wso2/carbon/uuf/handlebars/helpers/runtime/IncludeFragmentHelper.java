package org.wso2.carbon.uuf.handlebars.helpers.runtime;

import com.github.jknack.handlebars.Handlebars;
import com.github.jknack.handlebars.Helper;
import com.github.jknack.handlebars.Options;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import org.wso2.carbon.uuf.core.API;
import org.wso2.carbon.uuf.core.ComponentLookup;
import org.wso2.carbon.uuf.core.Fragment;
import org.wso2.carbon.uuf.core.RequestLookup;
import org.wso2.carbon.uuf.handlebars.model.ContextModel;
import org.wso2.carbon.uuf.model.Model;

import java.io.IOException;
import java.util.Optional;

import static org.wso2.carbon.uuf.handlebars.HbsRenderable.DATA_KEY_API;
import static org.wso2.carbon.uuf.handlebars.HbsRenderable.DATA_KEY_LOOKUP;
import static org.wso2.carbon.uuf.handlebars.HbsRenderable.DATA_KEY_REQUEST_LOOKUP;

public class IncludeFragmentHelper implements Helper<String> {

    public static final String HELPER_NAME = "includeFragment";
    private static final Logger log = LoggerFactory.getLogger(IncludeFragmentHelper.class);

    @Override
    public CharSequence apply(String fragmentName, Options options) throws IOException {
        ComponentLookup lookup = options.data(DATA_KEY_LOOKUP);
        Optional<Fragment> fragment = lookup.getFragment(fragmentName);
        if (!fragment.isPresent()) {
            throw new IllegalArgumentException("Fragment '" + fragmentName + "' does not exists in Component '" +
                                                       lookup.getComponentName() + "' or in its dependencies.");
        }

        if (log.isDebugEnabled()) {
            log.debug("Fragment \"" + fragment.get() + "\" is called from '" + options.fn.text() + "'.");
        }

        Model model = new ContextModel(options.context, options.hash);
        RequestLookup requestLookup = options.data(DATA_KEY_REQUEST_LOOKUP);
        API api = options.data(DATA_KEY_API);
        String content = fragment.get().render(model, lookup, requestLookup, api);
        return new Handlebars.SafeString(content);
    }
}
