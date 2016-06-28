/*
 *  Copyright (c) 2016, WSO2 Inc. (http://www.wso2.org) All Rights Reserved.
 *
 *  Licensed under the Apache License, Version 2.0 (the "License");
 *  you may not use this file except in compliance with the License.
 *  You may obtain a copy of the License at
 *
 *  http://www.apache.org/licenses/LICENSE-2.0
 *
 *  Unless required by applicable law or agreed to in writing, software
 *  distributed under the License is distributed on an "AS IS" BASIS,
 *  WITHOUT WARRANTIES OR CONDITIONS OF ANY KIND, either express or implied.
 *  See the License for the specific language governing permissions and
 *  limitations under the License.
 */

package org.wso2.carbon.uuf.renderablecreator.hbs.internal;

import com.github.jknack.handlebars.Context;
import com.google.gson.Gson;
import com.google.gson.GsonBuilder;
import com.google.gson.JsonElement;
import com.google.gson.JsonObject;
import com.google.gson.JsonSerializationContext;
import com.google.gson.JsonSerializer;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

import java.lang.reflect.Field;
import java.lang.reflect.Type;
import java.util.Map;
import java.util.Set;

// TODO: 6/27/16 refactor this class for proper debugging
public class DebugUtil {

    private static final Logger log = LoggerFactory.getLogger(DebugUtil.class);
    private static final Gson gson;

    static {
        GsonBuilder gsonBuilder = new GsonBuilder();
        gsonBuilder.registerTypeAdapter(Context.class, new HbsContextSerializer());
        gson = gsonBuilder.create();
    }

    public static String safeJsonString(Object obj) {
        try {
            return gson.toJson(obj);
        } catch (Throwable e) {
            log.debug("Un-serializable object detected " + obj, e);
            return "{\"__uuf_error__\":true}";
        }
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
                    JsonObject extendedContextJson = jsonSerializationContext.serialize(extendedContext.model())
                            .getAsJsonObject();

                    Set<Map.Entry<String, JsonElement>> entries = extendedContextJson.entrySet();
                    for (Map.Entry<String, JsonElement> entry : entries) {
                        serialized.add(entry.getKey(), entry.getValue());
                    }
                }
            } catch (NoSuchFieldException | IllegalAccessException e) {
                log.error("Error while serializing the handlebars context: " + context);
            }
            return serialized;
        }
    }
}
