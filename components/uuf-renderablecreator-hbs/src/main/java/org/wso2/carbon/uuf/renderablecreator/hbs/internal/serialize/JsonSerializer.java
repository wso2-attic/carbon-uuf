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
import com.google.gson.Gson;
import com.google.gson.GsonBuilder;
import jdk.nashorn.api.scripting.ScriptObjectMirror;

/**
 * JSON serializer for JavaScript object of Nashorn.
 *
 * @since 1.0.0
 */
public class JsonSerializer {

    private static final Gson safeSerializer, prettySerializer;

    static {
        safeSerializer = new GsonBuilder()
                .registerTypeAdapter(ScriptObjectMirror.class, new ScriptObjectMirrorSerializer())
                .create();
        prettySerializer = new GsonBuilder()
                .registerTypeAdapter(ScriptObjectMirror.class, new FunctionScriptObjectMirrorSerializer())
                .registerTypeAdapter(Context.class, new HbsContextSerializer())
                .setPrettyPrinting()
                .disableHtmlEscaping()
                .serializeNulls()
                .create();
    }

    /**
     * Converts the given object to JSON. The returned JSON string doesn't include any {@code null} values/fields
     * and any HTML special characters (e.g. &gt;) will be escaped with unicode characters.
     *
     * @param src object to be serialized to JSON
     * @return JSON representation of {@code src}
     */
    public static String toSafeJson(Object src) {
        return safeSerializer.toJson(src);
    }

    /**
     * Converts the given object to JSON. The returned JSON string includes indentations and new lines to for
     * formatting, has any null values/fields of the object, and any HTML special characters won't be escaped.
     *
     * @param src object to be serialized to JSON
     * @return JSON representation of {@code src}
     */
    public static String toPrettyJson(Object src) {
        return prettySerializer.toJson(src);
    }
}
