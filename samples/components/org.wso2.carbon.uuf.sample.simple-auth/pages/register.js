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
        var password = env.request.formParams['input-password'];
        var confirmPassword = env.request.formParams['input-confirm-password'];
        var result = validateData(password, confirmPassword);
        if (result.success) {
            sendRedirect(env.contextPath + env.config['registerRedirectUri']);
        } else {
            return {errorMessage: result.message};
        }
    }
}

function validateData(password, confirmPassowrd) {
    if (password == '') {
        return {success: false, message: "Please enter a password to continue registration."};
    } else if (password != confirmPassowrd) {
        return {success: false, message: "Incorrect password confirmation."};
    }
    return {success: true, message: "success"}
}