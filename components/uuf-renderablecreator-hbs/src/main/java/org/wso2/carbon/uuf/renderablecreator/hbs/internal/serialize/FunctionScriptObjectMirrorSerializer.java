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

import com.google.gson.JsonElement;
import com.google.gson.JsonPrimitive;
import com.google.gson.JsonSerializationContext;
import jdk.nashorn.api.scripting.ScriptObjectMirror;

import java.lang.reflect.Type;

/**
 * JSON serializer for function {@link ScriptObjectMirror} objects.
 * <p>
 * For JS functions, this serializer outputs the function signature.
 *
 * @since 1.0.0
 */
public class FunctionScriptObjectMirrorSerializer extends ScriptObjectMirrorSerializer {

    /**
     * {@inheritDoc}
     */
    @Override
    public JsonElement serialize(ScriptObjectMirror jsObj, Type type, JsonSerializationContext context) {
        if ((jsObj != null) && jsObj.isFunction()) {
            String functionSource = jsObj.toString();
            int openCurlyBraceIndex = functionSource.indexOf('{');
            if (openCurlyBraceIndex == -1) {
                return new JsonPrimitive("function ()");
            } else {
                return new JsonPrimitive(functionSource.substring(0, openCurlyBraceIndex).trim());
            }
        } else {
            return super.serialize(jsObj, type, context);
        }
    }
}
