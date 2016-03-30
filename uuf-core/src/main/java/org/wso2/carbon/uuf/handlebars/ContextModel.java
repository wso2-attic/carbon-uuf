package org.wso2.carbon.uuf.handlebars;

import com.github.jknack.handlebars.Context;
import org.wso2.carbon.uuf.core.Model;

import java.util.Map;


public class ContextModel implements Model {
    private Context context;


    public ContextModel(Context context) {
        this.context = context;
    }

    public static ContextModel from(Model model) {
        if (model instanceof ContextModel) {
            return (ContextModel) model;
        } else {
            Context context = Context.newContext(model.toMap());
            return new ContextModel(context);
        }
    }

    public Context getContext() {
        return context;
    }

    @Override
    public void combine(Map<String, Object> other) {

    }

    @Override
    public Map<String, Object> toMap() {
        throw new UnsupportedOperationException();
    }
}
