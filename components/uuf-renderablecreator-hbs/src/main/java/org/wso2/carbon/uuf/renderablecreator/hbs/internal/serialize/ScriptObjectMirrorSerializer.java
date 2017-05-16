/*
 * Copyright (c) 2017, WSO2 Inc. (http://www.wso2.org) All Rights Reserved.
 *
 * WSO2 Inc. licenses this file to you under the Apache License,
 * Version 2.0 (the "License"); you may not use this file except
 * in compliance with the License.
 * You may obtain a copy of the License at
 *
 * http://www.apache.org/licenses/LICENSE-2.0
 *
 * Unless required by applicable law or agreed to in writing,
 * software distributed under the License is distributed on an
 * "AS IS" BASIS, WITHOUT WARRANTIES OR CONDITIONS OF ANY
 * KIND, either express or implied.  See the License for the
 * specific language governing permissions and limitations
 * under the License.
 */

package org.wso2.carbon.uuf.renderablecreator.hbs.internal.serialize;

import com.google.gson.JsonArray;
import com.google.gson.JsonElement;
import com.google.gson.JsonNull;
import com.google.gson.JsonObject;
import com.google.gson.JsonSerializationContext;
import com.google.gson.JsonSerializer;
import jdk.nashorn.api.scripting.ScriptObjectMirror;

import java.lang.reflect.Type;

/**
 * JSON serializer for {@link ScriptObjectMirror} objects.
 *
 * @since 1.0.0
 */
public class ScriptObjectMirrorSerializer implements JsonSerializer<ScriptObjectMirror> {

    /**
     * {@inheritDoc}
     */
    @Override
    public JsonElement serialize(ScriptObjectMirror jsObj, Type type, JsonSerializationContext context) {
        if ((jsObj == null) || ScriptObjectMirror.isUndefined(jsObj) || jsObj.isFunction()) {
            return JsonNull.INSTANCE;
        }

        if (jsObj.isArray()) {
            JsonArray jsonArray = new JsonArray();
            for (Object item : jsObj.values()) {
                jsonArray.add(serializeFurther(context.serialize(item), context));
            }
            return jsonArray;
        }
        if (jsObj.isEmpty()) {
            return new JsonObject();
        }

        JsonObject jsonObject = new JsonObject();
        for (String key : jsObj.getOwnKeys(false)) {
            jsonObject.add(key, serializeFurther(jsObj.getMember(key), context));
        }
        return jsonObject;
    }

    private JsonElement serializeFurther(Object src, JsonSerializationContext context) {
        return ScriptObjectMirror.isUndefined(src) ? JsonNull.INSTANCE : context.serialize(src);
    }
}
