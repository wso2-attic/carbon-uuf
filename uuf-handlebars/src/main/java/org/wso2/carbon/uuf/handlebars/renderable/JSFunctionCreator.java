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

package org.wso2.carbon.uuf.handlebars.renderable;

import com.google.gson.Gson;
import org.wso2.carbon.uuf.api.Placeholder;
import org.wso2.carbon.uuf.api.auth.Session;
import org.wso2.carbon.uuf.core.API;

import java.util.Map;

public class JSFunctionCreator {

    private static final CallOSGiServiceFunction CALL_OSGI_SERVICE_FUNCTION;
    private static final GetOSGiServicesFunction GET_OSGI_SERVICES_FUNCTION;
    private static final CallMicroServiceFunction CALL_MICRO_SERVICE_FUNCTION;
    private static final SendErrorFunction SEND_ERROR_FUNCTION;
    private static final SendRedirectFunction SEND_REDIRECT_FUNCTION;
    private static final Gson GSON;

    private final API api;
    private CreateSessionFunction createSessionFunction;
    private GetSessionFunction getSessionFunction;
    private DestroySessionFunction destroySessionFunction;
    private SetAppThemeFunction setAppThemeFunction;
    private GetAppThemeFunction getAppThemeFunction;
    private SendToClientFunction sendToClientFunction;

    static {
        CALL_OSGI_SERVICE_FUNCTION = API::callOSGiService;
        GET_OSGI_SERVICES_FUNCTION = API::getOSGiServices;
        CALL_MICRO_SERVICE_FUNCTION = API::callMicroService;
        SEND_ERROR_FUNCTION = API::sendError;
        SEND_REDIRECT_FUNCTION = API::sendRedirect;
        GSON = new Gson();
    }

    public JSFunctionCreator(API api) {
        this.api = api;
    }

    public static CallOSGiServiceFunction getCallOsgiServiceFunction() {
        return CALL_OSGI_SERVICE_FUNCTION;
    }

    public static GetOSGiServicesFunction getGetOsgiServicesFunction() {
        return GET_OSGI_SERVICES_FUNCTION;
    }

    public static CallMicroServiceFunction getCallMicroServiceFunction() {
        return CALL_MICRO_SERVICE_FUNCTION;
    }

    public static SendErrorFunction getSendErrorFunction() {
        return SEND_ERROR_FUNCTION;
    }

    public static SendRedirectFunction getSendRedirectFunction() {
        return SEND_REDIRECT_FUNCTION;
    }

    public CreateSessionFunction getCreateSessionFunction() {
        if (createSessionFunction == null) {
            createSessionFunction = api::createSession;
        }
        return createSessionFunction;
    }

    public GetSessionFunction getGetSessionFunction() {
        if (getSessionFunction == null) {
            getSessionFunction = () -> api.getSession().orElse(null);
        }
        return getSessionFunction;
    }

    public DestroySessionFunction getDestroySessionFunction() {
        if (destroySessionFunction == null) {
            destroySessionFunction = api::destroySession;
        }
        return destroySessionFunction;
    }

    public SetAppThemeFunction getSetAppThemeFunction() {
        if (setAppThemeFunction == null) {
            setAppThemeFunction = api::setAppTheme;
        }
        return setAppThemeFunction;
    }

    public GetAppThemeFunction getGetAppThemeFunction() {
        if (getAppThemeFunction == null) {
            getAppThemeFunction = () -> api.getAppTheme().orElse(null);
        }
        return getAppThemeFunction;
    }

    public SendToClientFunction getSendToClientFunction() {
        if (sendToClientFunction == null) {
            sendToClientFunction = (name, value) -> {
                String scriptTag = "<script type=\"text/javascript\">var " + name + "=" + GSON.toJson(value) +
                        ";</script>";
                api.getRequestLookup().addToPlaceholder(Placeholder.js, scriptTag);
            };
        }
        return sendToClientFunction;
    }

    @FunctionalInterface
    public interface CallOSGiServiceFunction {

        @SuppressWarnings("unused")
        Object call(String serviceClassName, String serviceMethodName, Object... args);
    }

    @FunctionalInterface
    public interface GetOSGiServicesFunction {

        @SuppressWarnings("unused")
        Map<String, Object> call(String serviceClassName);
    }

    @FunctionalInterface
    public interface CallMicroServiceFunction {

        @SuppressWarnings("unused")
        void call();
    }

    @FunctionalInterface
    public interface SendErrorFunction {

        @SuppressWarnings("unused")
        void call(int status, String message);
    }

    @FunctionalInterface
    public interface SendRedirectFunction {

        @SuppressWarnings("unused")
        void call(String redirectUrl);
    }

    @FunctionalInterface
    public interface CreateSessionFunction {

        @SuppressWarnings("unused")
        Session call(String userName);
    }

    @FunctionalInterface
    public interface GetSessionFunction {

        @SuppressWarnings("unused")
        Session call();
    }

    @FunctionalInterface
    public interface DestroySessionFunction {

        @SuppressWarnings("unused")
        boolean call();
    }

    @FunctionalInterface
    public interface SetAppThemeFunction {

        @SuppressWarnings("unused")
        void call(String themeName);
    }

    @FunctionalInterface
    public interface GetAppThemeFunction {

        @SuppressWarnings("unused")
        String call();
    }

    @FunctionalInterface
    public interface SendToClientFunction {

        @SuppressWarnings("unused")
        void call(String name, Object value);
    }
}
