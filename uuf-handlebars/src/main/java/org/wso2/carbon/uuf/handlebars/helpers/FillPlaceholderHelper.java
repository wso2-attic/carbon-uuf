package org.wso2.carbon.uuf.handlebars.helpers;

import com.github.jknack.handlebars.Helper;

public abstract class FillPlaceholderHelper implements Helper<String> {

    protected final String placeholderName;

    protected FillPlaceholderHelper(String placeholderName) {
        this.placeholderName = placeholderName;
    }

    public String getPlaceholderName() {
        return placeholderName;
    }
}
