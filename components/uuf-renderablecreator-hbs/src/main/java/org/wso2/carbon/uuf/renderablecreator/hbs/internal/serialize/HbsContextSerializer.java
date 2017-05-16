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

import com.github.jknack.handlebars.Context;
import com.google.gson.JsonElement;
import com.google.gson.JsonObject;
import com.google.gson.JsonSerializationContext;
import com.google.gson.JsonSerializer;

import java.lang.reflect.Field;
import java.lang.reflect.Type;

/**
 * JSON Serializer for Handlebars {@link Context} objects.
 *
 * @since 1.0.0
 */
public class HbsContextSerializer implements JsonSerializer<Context> {

    /**
     * {@inheritDoc}
     */
    @Override
    public JsonElement serialize(Context hbsContext, Type type, JsonSerializationContext serializationContext) {
        JsonObject serialized = serializationContext.serialize(hbsContext.model()).getAsJsonObject();

        Context extendedContext;
        try {
            Field extendedContextField = ((Class) type).getDeclaredField("extendedContext");
            extendedContextField.setAccessible(true);
            extendedContext = (Context) extendedContextField.get(hbsContext);
        } catch (NoSuchFieldException | IllegalAccessException e) {
            return serialized;
        }
        if (extendedContext == null) {
            return serialized;
        }

        serializationContext.serialize(extendedContext.model()).getAsJsonObject().entrySet()
                .forEach(entry -> serialized.add(entry.getKey(), entry.getValue()));
        return serialized;
    }
}
