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

function onRequest(env) {
    if (env.request.method == "POST") {

        var uploadedFile = env.request.files["file-content"];
        var FileUtils = Java.type("org.wso2.carbon.uuf.sample.featuresapp.bundle.FileUtils");

        try {
            var tempDirPath = FileUtils.copy(uploadedFile.path, uploadedFile.name);
            return {
                message: 'You have successfully uploaded the file, ' + uploadedFile.name + ' and copied it to '
                         + tempDirPath + ' directory.'
            };
        } catch (e) {
            Log.error("Error occurred while copying the file.", e);
            return {
                error: "Error occurred while uploading the file."
            };
        }
    }
}
