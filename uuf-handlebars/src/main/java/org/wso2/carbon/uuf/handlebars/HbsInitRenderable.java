package org.wso2.carbon.uuf.handlebars;

import com.github.jknack.handlebars.Context;
import com.github.jknack.handlebars.Handlebars;
import com.github.jknack.handlebars.io.TemplateSource;
import org.wso2.carbon.uuf.core.exception.UUFException;
import org.wso2.carbon.uuf.handlebars.helpers.init.LayoutHelper;

import java.io.IOException;
import java.util.Collections;
import java.util.Optional;

public class HbsInitRenderable {

    public static final String DATA_KEY_CURRENT_LAYOUT = HbsInitRenderable.class.getName() + "#layout";
    private static final Handlebars HANDLEBARS = new Handlebars();

    static {
        HANDLEBARS.registerHelper(LayoutHelper.HELPER_NAME, new LayoutHelper());
        HANDLEBARS.registerHelperMissing((context, options) -> "");
    }

    private final Optional<String> layout;

    public HbsInitRenderable(TemplateSource template) {
        String templatePath = template.filename();
        Context context = Context.newContext(Collections.emptyMap());
        try {
            HANDLEBARS.compile(template).apply(context);
        } catch (IOException e) {
            throw new UUFException(
                    "An error occurred when pre-processing the Handlebars template of page '" + templatePath + "'.", e);
        }
        layout = Optional.ofNullable(context.data(DATA_KEY_CURRENT_LAYOUT));
    }

    public Optional<String> getLayoutName() {
        return layout;
    }
}
