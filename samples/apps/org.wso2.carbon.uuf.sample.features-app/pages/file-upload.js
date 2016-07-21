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
        var x = env.request.formParams['file-name'];
        var y = env.request.files['file-name'];
        var txt = "";
        if (x.type === 'String' || x.type ==='files') {
            //     if (x.files.length == 0) {
            //         txt = "Select one or more files.";
            //     } else {
            //         // for (var i = 0; i < x.files.length; i++) {
            //         //     txt += "<br><strong>" + (i+1) + ". file</strong><br>";
            //         //     var file = x.files[i];
            //         //     if ('name' in file) {
            //         //         txt += "name: " + file.name + "<br>";
            //         //     }
            //         //     if ('size' in file) {
            //         //         txt += "size: " + file.size + " bytes <br>";
            //         //     }
            //         // }
            txt= "file temp";
        }
        return {message: 'You have successfully uploaded the file,' + x+ '. ' + txt};
    }
}