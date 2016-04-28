package org.wso2.carbon.uuf.handlebars.model;

import com.github.jknack.handlebars.Context;
import org.wso2.carbon.uuf.model.MapModel;

import java.util.HashMap;
import java.util.Map;

public class ContextModel extends MapModel {

    private Context parentContext;

    public ContextModel(Context context) {
        this(context, new HashMap<>());
    }

    public ContextModel(Context parentContext, Map<String, Object> map) {
        super(map);
        this.parentContext = parentContext;
    }

    public Context getParentContext() {
        return parentContext;
    }
}
