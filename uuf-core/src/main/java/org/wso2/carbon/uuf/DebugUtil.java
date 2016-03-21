package org.wso2.carbon.uuf;

import com.github.jknack.handlebars.Context;
import com.google.gson.*;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

import java.lang.reflect.Field;
import java.lang.reflect.Type;
import java.util.Iterator;
import java.util.Map;
import java.util.Set;

public class DebugUtil {
    private static final Logger log = LoggerFactory.getLogger(DebugUtil.class);

    private static final Gson gson;

    static {
        GsonBuilder gsonBuilder = new GsonBuilder();
        gsonBuilder.registerTypeAdapter(Context.class, new HbsContextSerializer());
        gson = gsonBuilder.create();
    }

    private static class HbsContextSerializer implements JsonSerializer<Context> {

        @Override
        public JsonElement serialize(Context context, Type type, JsonSerializationContext jsonSerializationContext) {
            JsonObject serialized = jsonSerializationContext.serialize(context.model()).getAsJsonObject();
            try {
                Field extendedContextField = ((Class) type).getDeclaredField("extendedContext");
                extendedContextField.setAccessible(true);
                Context extendedContext = (Context) extendedContextField.get(context);
                if (extendedContext != null) {
                    JsonObject extendedContextJson = jsonSerializationContext.serialize(extendedContext.model()).getAsJsonObject();

                    Set<Map.Entry<String, JsonElement>> entries = extendedContextJson.entrySet();
                    for (Map.Entry<String, JsonElement> entry : entries) {
                        serialized.add(entry.getKey(), entry.getValue());
                    }
                }
            } catch (NoSuchFieldException | IllegalAccessException e) {
                e.printStackTrace();
            }
            return serialized;
        }
    }

    public static String safeJsonString(Object obj) {
        try {
            return gson.toJson(obj);
        } catch (Error error) {
            log.debug("Un-serializable object detected " + obj, error);
            return "{\"__uuf_error__\":true}";
        }
    }

}
