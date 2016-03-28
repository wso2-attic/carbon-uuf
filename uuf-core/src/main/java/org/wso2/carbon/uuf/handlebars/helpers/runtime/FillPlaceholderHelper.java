package org.wso2.carbon.uuf.handlebars.helpers.runtime;

import com.github.jknack.handlebars.Context;
import com.github.jknack.handlebars.Helper;
import com.github.jknack.handlebars.Options;

import java.util.Optional;

/**
 * @param <T> placeholder value type
 */
public abstract class FillPlaceholderHelper<T> implements Helper<String> {

    private final String placeholderName;

    public FillPlaceholderHelper(String placeholderName) {
        this.placeholderName = this.getClass().getName() + "#" + placeholderName;
    }

    public abstract Optional<String> getPlaceholderValue(Context handlebarsContext);

    protected Optional<T> getValue(Options handlebarsHelperOptions) {
        return Optional.ofNullable(handlebarsHelperOptions.data(this.placeholderName));
    }

    protected Optional<T> getValue(Context handlebarsContext) {
        return Optional.ofNullable(handlebarsContext.data(this.placeholderName));
    }

    /**
     * @param handlebarsHelperOptions options of a Handlebars helper
     * @param value                   value to be set
     * @return set value
     */
    protected T setValue(Options handlebarsHelperOptions, T value) {
        handlebarsHelperOptions.data(placeholderName, value);
        return value;
    }
}
