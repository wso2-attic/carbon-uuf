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

//noinspection JSUnusedGlobalSymbols
function onGet(env) {
    var devices;
    if ((env.params.offset && env.params.limit)) {
        devices = getDevices(env.params.offset, env.params.limit);
    } else {
        devices = getDevices(0, 0);
    }

    return {"devices": devices}
}

function getDevices(offset, limit) {
    offset = parseInt(offset);
    limit = parseInt(limit);
    var lastIndex = offset + limit;
    var result = [{name: "device1", type: "android"}, {name: "device2", type: "windows"},{name: "device3", type: "ios"}];
    if (limit == 0) {
        //no pagination
        return result;
    } else if (lastIndex <= result.length) {
        //valid pagination
        result = result.slice(offset, lastIndex);
    } else {
        //invalid pagination
        result = [];
    }
    return result;
}