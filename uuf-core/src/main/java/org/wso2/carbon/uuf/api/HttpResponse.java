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

package org.wso2.carbon.uuf.api;

import java.io.File;
import java.io.InputStream;
import java.nio.file.Path;
import java.util.Map;

public interface HttpResponse {

    void setStatus(int statusCode);

    void setContent(String content);

    void setContent(String content, String contentType);

    void setContent(File content);

    void setContent(File content, String contentType);

    void setContent(Path content);

    void setContent(Path content, String contentType);

    void setContent(InputStream content, String contentType);

    Object getContent();

    void setContentType(String type);

    String getContentType();

    void setHeader(String name, String value);

    Map<String, String> getHeaders();
}
