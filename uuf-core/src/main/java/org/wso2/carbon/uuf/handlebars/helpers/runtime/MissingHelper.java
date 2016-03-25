package org.wso2.carbon.uuf.handlebars.helpers.runtime;

import com.github.jknack.handlebars.Helper;
import com.github.jknack.handlebars.Options;
import com.google.common.collect.ImmutableSet;
import org.wso2.carbon.uuf.core.UUFException;

import java.io.IOException;

import static org.wso2.carbon.uuf.handlebars.HbsInitRenderable.HELPER_NAME_FILL_ZONE;
import static org.wso2.carbon.uuf.handlebars.HbsInitRenderable.HELPER_NAME_LAYOUT;

public class MissingHelper implements Helper {

    private static final ImmutableSet<String> KEYWORDS = ImmutableSet.of(HELPER_NAME_LAYOUT, HELPER_NAME_FILL_ZONE);

    @Override
    public CharSequence apply(Object arg, Options options) throws IOException {
        if (KEYWORDS.contains(options.helperName)) {
            return "";
        }
        throw new UUFException("Cannot evaluate the variable/helper '" + options.helperName + "'.");
    }
}
