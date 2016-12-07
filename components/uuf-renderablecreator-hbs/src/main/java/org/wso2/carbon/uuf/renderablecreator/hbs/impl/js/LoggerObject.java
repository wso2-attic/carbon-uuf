/*
 * Copyright (c) 2016, WSO2 Inc. (http://www.wso2.org) All Rights Reserved.
 *
 * Licensed under the Apache License, Version 2.0 (the "License");
 * you may not use this file except in compliance with the License.
 * You may obtain a copy of the License at
 *
 * http://www.apache.org/licenses/LICENSE-2.0
 *
 * Unless required by applicable law or agreed to in writing, software
 * distributed under the License is distributed on an "AS IS" BASIS,
 * WITHOUT WARRANTIES OR CONDITIONS OF ANY KIND, either express or implied.
 * See the License for the specific language governing permissions and
 * limitations under the License.
 */

package org.wso2.carbon.uuf.renderablecreator.hbs.impl.js;

import com.google.gson.Gson;
import com.google.gson.GsonBuilder;
import com.google.gson.JsonArray;
import com.google.gson.JsonElement;
import com.google.gson.JsonNull;
import com.google.gson.JsonObject;
import com.google.gson.JsonPrimitive;
import com.google.gson.JsonSerializationContext;
import com.google.gson.JsonSerializer;
import jdk.nashorn.api.scripting.ScriptObjectMirror;

import java.lang.reflect.Type;

public class LoggerObject {

    public static final String NAME = "Log";
    private static final Gson GSON;

    private final org.slf4j.Logger logger;

    static {
        GsonBuilder gsonBuilder = new GsonBuilder();
        gsonBuilder.registerTypeAdapter(ScriptObjectMirror.class, new ScriptObjectMirrorSerializer());
        GSON = gsonBuilder.create();
    }

    LoggerObject(String name) {
        this.logger = org.slf4j.LoggerFactory.getLogger(name);
    }

    private String getLogMessage(Object obj) {
        if (obj instanceof ScriptObjectMirror) {
            return GSON.toJson((ScriptObjectMirror) obj);
        } else {
            return GSON.toJson(ScriptObjectMirrorSerializer.serialize(obj));
        }
    }

    public void info(Object obj) {
        logger.info(getLogMessage(obj));
    }

    public void debug(Object obj) {
        logger.debug(getLogMessage(obj));
    }

    public void trace(Object obj) {
        logger.trace(getLogMessage(obj));
    }

    public void warn(Object obj) {
        logger.warn(getLogMessage(obj));
    }

    public void error(Object obj) {
        logger.error(getLogMessage(obj));
    }

    private static class ScriptObjectMirrorSerializer implements JsonSerializer<ScriptObjectMirror> {

        @Override
        public JsonElement serialize(ScriptObjectMirror jsObj, Type type,
                                     JsonSerializationContext serializationContext) {
            return serialize(jsObj, serializationContext);
        }

        private JsonElement serialize(ScriptObjectMirror jsObj, JsonSerializationContext serializationContext) {
            if (jsObj == null) {
                return JsonNull.INSTANCE;
            }
            if (jsObj.isFunction()) {
                String functionSource = jsObj.toString();
                int openCurlyBraceIndex = functionSource.indexOf('{');
                if (openCurlyBraceIndex == -1) {
                    return new JsonPrimitive("function ()");
                } else {
                    return new JsonPrimitive(functionSource.substring(0, openCurlyBraceIndex).trim());
                }
            }
            if (jsObj.isArray()) {
                JsonArray jsonArray = new JsonArray();
                for (Object item : jsObj.values()) {
                    if (item instanceof ScriptObjectMirror) {
                        jsonArray.add(serialize((ScriptObjectMirror) item, serializationContext));
                    } else {
                        jsonArray.add(serialize(item));
                    }
                }
                return jsonArray;
            }
            if (jsObj.isEmpty()) {
                return new JsonObject();
            } else {
                JsonObject jsonObject = new JsonObject();
                for (String key : jsObj.getOwnKeys(true)) {
                    Object member = jsObj.getMember(key);
                    if (member instanceof ScriptObjectMirror) {
                        jsonObject.add(key, serialize((ScriptObjectMirror) member, serializationContext));
                    } else {
                        jsonObject.add(key, serialize(member));
                    }
                }
                return jsonObject;
            }
        }

        public static JsonElement serialize(Object obj) {
            if (obj == null) {
                return JsonNull.INSTANCE;
            }
            if (ScriptObjectMirror.isUndefined(obj)) {
                return new JsonPrimitive("undefined");
            }
            if (obj instanceof Boolean) {
                return new JsonPrimitive((Boolean) obj);
            }
            if (obj instanceof Number) {
                return new JsonPrimitive((Number) obj);
            }
            if (obj instanceof Character) {
                return new JsonPrimitive((Character) obj);
            }
            if (obj instanceof String) {
                return new JsonPrimitive((String) obj);
            } else {
                return new JsonPrimitive("{" + obj.getClass().getName() + "}");
            }
        }
    }

    @Override
    public String toString() {
        return "{info: function(obj), debug: function(obj), trace: function(obj), warn: function(obj), error: " +
                "function(obj)}";
    }
}
