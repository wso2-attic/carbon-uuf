package org.wso2.carbon.uuf.handlebars.helpers.runtime;

import com.github.jknack.handlebars.Helper;
import com.github.jknack.handlebars.Options;
import com.google.common.collect.ImmutableSet;
import org.wso2.carbon.uuf.core.exception.UUFException;
import org.wso2.carbon.uuf.handlebars.helpers.init.FillZoneHelper;
import org.wso2.carbon.uuf.handlebars.helpers.init.LayoutHelper;

import java.io.IOException;

public class MissingHelper implements Helper {

    private static final ImmutableSet<String> KEYWORDS = ImmutableSet.of(LayoutHelper.HELPER_NAME,
                                                                         FillZoneHelper.HELPER_NAME);

    @Override
    public CharSequence apply(Object arg, Options options) throws IOException {
        if (KEYWORDS.contains(options.helperName)) {
            return "";
        }
        throw new UUFException("Cannot evaluate the variable/helper '" + options.helperName + "'.");
    }
}
