package org.wso2.carbon.uuf.handlebars.helpers.runtime;

import com.github.jknack.handlebars.Context;
import com.github.jknack.handlebars.Options;

import java.io.IOException;
import java.util.Optional;

public class HeaderTitleHelper extends FillPlaceholderHelper<String> {
    public static final String HELPER_NAME = "headerTitle";

    public HeaderTitleHelper() {
        super(HELPER_NAME);
    }

    public CharSequence apply(String title, Options options) throws IOException {
        if (getValue(options).isPresent()) {
            throw new IllegalStateException("Page header title is already set.");
        }
        setValue(options, title);
        return "";
    }

    @Override
    public Optional<String> getPlaceholderValue(Context handlebarsContext) {
        return getValue(handlebarsContext);
    }
}
