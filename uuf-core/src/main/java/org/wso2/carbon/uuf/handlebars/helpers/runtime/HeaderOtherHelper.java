package org.wso2.carbon.uuf.handlebars.helpers.runtime;

import com.github.jknack.handlebars.Context;
import com.github.jknack.handlebars.Options;

import java.io.IOException;
import java.util.ArrayList;
import java.util.List;
import java.util.Optional;

public class HeaderOtherHelper extends FillPlaceholderHelper<List<String>> {

    public static final String HELPER_NAME = "headerOther";

    public HeaderOtherHelper() {
        super(HELPER_NAME);
    }

    @Override
    public CharSequence apply(String s, Options options) throws IOException {
        List<String> buffer = getValue(options).orElse(setValue(options, new ArrayList<>()));
        buffer.add(options.fn().toString());
        return "";
    }

    @Override
    public Optional<String> getPlaceholderValue(Context handlebarsContext) {
        return getValue(handlebarsContext).map(values -> {
            StringBuilder tmpBuffer = new StringBuilder();
            for (String item : values) {
                tmpBuffer.append(item);
            }
            return tmpBuffer.toString();
        });
    }
}
