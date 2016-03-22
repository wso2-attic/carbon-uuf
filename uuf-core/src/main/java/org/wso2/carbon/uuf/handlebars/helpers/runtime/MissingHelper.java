package org.wso2.carbon.uuf.handlebars.helpers.runtime;

import com.github.jknack.handlebars.Helper;
import com.github.jknack.handlebars.Options;
import com.google.common.collect.ImmutableSet;
import org.wso2.carbon.uuf.core.UUFException;

import java.io.IOException;

public class MissingHelper implements Helper {

    private static final ImmutableSet<String> KEYWORDS = ImmutableSet.of("layout", "fillZone", "headCss");
    public static final MissingHelper INSTANCE = new MissingHelper();

    @Override
    public CharSequence apply(Object arg, Options options) throws IOException {

        if (KEYWORDS.contains(options.helperName)) {
            return "";
        }
        throw new UUFException("value not available for the variable/helper '" + options.helperName + "' in " + arg);
    }
}
