package org.wso2.carbon.uuf.handlebars.helpers.runtime;

import com.github.jknack.handlebars.Context;
import com.github.jknack.handlebars.Helper;

import java.util.List;
import java.util.Optional;

public abstract class FillPlaceholderHelper implements Helper<String> {

    protected final String placeholderName;

    public FillPlaceholderHelper(String placeholderName) {
        this.placeholderName = this.getClass().getName() + "#" + placeholderName;
    }

    public Optional<String> getValue(Context context) {
        List<String> resourcesList = context.data(this.placeholderName);
        if (resourcesList == null || resourcesList.isEmpty()) {
            return Optional.<String>empty();
        }
        StringBuilder tmpBuffer = new StringBuilder();
        for (String item : resourcesList) {
            tmpBuffer.append(item);
        }
        return Optional.of(tmpBuffer.toString());
    }

    protected abstract String formatValue(String value);
}
