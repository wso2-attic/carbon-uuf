/*
 * Copyright (c) 2016, WSO2 Inc. (http://www.wso2.org) All Rights Reserved.
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

package org.wso2.carbon.uuf.api.config;

import com.google.common.collect.ImmutableListMultimap;
import com.google.common.collect.ListMultimap;

import java.util.List;
import java.util.Map;
import java.util.Optional;
import java.util.Set;

import static java.util.Collections.emptyMap;
import static java.util.Collections.emptySet;
import static java.util.Collections.unmodifiableMap;
import static java.util.Collections.unmodifiableSet;

/**
 * Represents the final configuration of an UUF App.
 *
 * @since 1.0.0
 */
public class Configuration {

    private String contextPath;
    private String themeName;
    private String loginPageUri;
    private Map<Integer, String> errorPageUris;
    private String defaultErrorPageUri;
    private ListMultimap<String, MenuItem> menus;
    private Set<String> acceptingCsrfPatterns;
    private Set<String> rejectingCsrfPatterns;
    private Set<String> acceptingXssPatterns;
    private Set<String> rejectingXssPatterns;
    private Map<String, String> responseHeaders;
    private Map<String, Object> otherConfigurations;

    protected Configuration() {
    }

    /**
     * Returns the configured client-side context path for the app.
     *
     * @return client-side context path
     */
    public Optional<String> getContextPath() {
        return Optional.ofNullable(contextPath);
    }

    /**
     * Sets the client-side context path for the app.
     *
     * @param contextPath client-side context path to be set
     * @throws IllegalArgumentException if the context path is empty or doesn't start with a '/'
     * @see #getContextPath()
     */
    protected void setContextPath(String contextPath) {
        if (contextPath != null) {
            if (contextPath.isEmpty()) {
                throw new IllegalArgumentException("Context path cannot be empty.");
            } else if (contextPath.charAt(0) != '/') {
                throw new IllegalArgumentException("Context path must start with a '/'. Instead found '" +
                                                           contextPath.charAt(0) + "' at the beginning.");
            }
        }
        this.contextPath = contextPath;
    }

    /**
     * Returns the configured default theme name for the app.
     *
     * @return default theme name
     */
    public Optional<String> getThemeName() {
        return Optional.ofNullable(themeName);
    }

    /**
     * Sets the theme name for the app.
     *
     * @param themeName theme name to be set
     * @throws IllegalArgumentException if the theme name is empty
     * @see #getThemeName()
     */
    protected void setThemeName(String themeName) {
        if ((themeName != null) && themeName.isEmpty()) {
            throw new IllegalArgumentException("Theme name cannot be empty.");
        }
        this.themeName = themeName;
    }

    /**
     * Returns the configured login page URI (without the app context path) for the app.
     *
     * @return login page's URI
     */
    public Optional<String> getLoginPageUri() {
        return Optional.ofNullable(loginPageUri);
    }

    /**
     * Sets the URI of the login page for the app.
     *
     * @param loginPageUri URI of the login page to be set
     * @throws IllegalArgumentException if login page URI is empty of doesn't start with a '/'
     * @see #getLoginPageUri()
     */
    protected void setLoginPageUri(String loginPageUri) {
        if (loginPageUri != null) {
            if (loginPageUri.isEmpty()) {
                throw new IllegalArgumentException("Login page URI cannot be empty.");
            } else if (loginPageUri.charAt(0) != '/') {
                throw new IllegalArgumentException("Login page URI must start with a '/'. Instead found '" +
                                                           loginPageUri.charAt(0) + "' at the beginning.");
            }
        }
        this.loginPageUri = loginPageUri;
    }

    /**
     * Returns the configured error page URI (without the app context path) for the specified HTTP status code.
     *
     * @param httpStatusCode HTTP status code
     * @return corresponding error page's URI
     * @see #getDefaultErrorPageUri()
     */
    public Optional<String> getErrorPageUri(int httpStatusCode) {
        return Optional.ofNullable(errorPageUris.get(httpStatusCode));
    }

    /**
     * Sets the URIs of the error pages for the app.
     *
     * @param errorPageUris URIs of the error pages to be set
     * @throws IllegalArgumentException if HTTP status code is less than 100 or greater than 999
     * @throws IllegalArgumentException if an error page URI is empty or doesn't start with a '/'
     * @see #getErrorPageUri(int)
     * @see #setDefaultErrorPageUri(String)
     */
    protected void setErrorPageUris(Map<Integer, String> errorPageUris) {
        if (errorPageUris == null) {
            this.errorPageUris = emptyMap();
        } else {
            for (Map.Entry<Integer, String> entry : errorPageUris.entrySet()) {
                int httpStatusCode = entry.getKey();
                String errorPageUri = entry.getValue();

                if ((httpStatusCode < 100) || (httpStatusCode > 999)) {
                    throw new IllegalArgumentException(
                            "HTTP status code of an error page entry must be between 100 and 999. Instead found '" +
                                    httpStatusCode + "' for URI '" + errorPageUri + "'.");
                }

                if (errorPageUri.isEmpty()) {
                    throw new IllegalArgumentException(
                            "URI of an error page entry cannot be empty. Found an empty URI for HTTP status code '" +
                                    httpStatusCode + "'.");
                } else if (errorPageUri.charAt(0) != '/') {
                    throw new IllegalArgumentException(
                            "URI of an error page entry must start with a '/'. Instead found '" +
                                    errorPageUri.charAt(0) + "' at the beginning of the URI for HTTP status code '" +
                                    httpStatusCode + "'.");
                }
            }
            this.errorPageUris = errorPageUris;
        }
    }

    /**
     * Return the configured default error page URI (without the app context path) for the app.
     *
     * @return default error page's URI
     * @see #getErrorPageUri(int)
     */
    public Optional<String> getDefaultErrorPageUri() {
        return Optional.ofNullable(defaultErrorPageUri);
    }

    /**
     * Sets the URI of the default error page for the app.
     *
     * @param defaultErrorPageUri URI of the default error page to be set
     * @throws IllegalArgumentException if default error page URI is empty for doesn't start with a '/'
     * @see #getDefaultErrorPageUri()
     * @see #setErrorPageUris(Map)
     */
    protected void setDefaultErrorPageUri(String defaultErrorPageUri) {
        if (defaultErrorPageUri != null) {
            if (defaultErrorPageUri.isEmpty()) {
                throw new IllegalArgumentException("URI of the default error page cannot be empty.");
            } else if (defaultErrorPageUri.charAt(0) != '/') {
                throw new IllegalArgumentException(
                        "URI of the default error page must start with a '/'. Instead found '" +
                                defaultErrorPageUri.charAt(0) + "' at the beginning.");
            }
        }
        this.defaultErrorPageUri = defaultErrorPageUri;
    }

    /**
     * Returns the configured menu items for the given name.
     *
     * @param name name of the menu
     * @return menu items for the specified name
     */
    public List<MenuItem> getMenu(String name) {
        return menus.get(name);
    }

    /**
     * Sets menu items for the app.
     *
     * @param menus menu items to be set
     * @see #getMenu(String)
     */
    protected void setMenus(Map<String, ? extends List<? extends MenuItem>> menus) {
        ImmutableListMultimap.Builder<String, MenuItem> builder = new ImmutableListMultimap.Builder<>();
        menus.forEach(builder::putAll);
        this.menus = builder.build();
    }

    /**
     * Returns the configured accepting CSRF URI patterns in the security configuration.
     *
     * @return accepting CSRF URI patterns
     * @see #getRejectingCsrfPatterns()
     */
    public Set<String> getAcceptingCsrfPatterns() {
        return acceptingCsrfPatterns;
    }

    /**
     * Sets the accepting CSRF patterns for the app.
     *
     * @param acceptingCsrfPatterns accepting CSRF patterns to be set
     * @throws IllegalArgumentException if a pattern is null or empty
     */
    protected void setAcceptingCsrfPatterns(Set<String> acceptingCsrfPatterns) {
        if (acceptingCsrfPatterns == null) {
            this.acceptingCsrfPatterns = emptySet();
        } else {
            for (String acceptingCsrfPattern : acceptingCsrfPatterns) {
                if (acceptingCsrfPattern == null) {
                    throw new IllegalArgumentException("An accepting CSRF pattern cannot be null.");
                } else if (acceptingCsrfPattern.isEmpty()) {
                    throw new IllegalArgumentException("An accepting CSRF pattern cannot be empty.");
                }
                // TODO: 12/31/16 check whether the 'acceptingCsrfPattern' is a valid URI pattern
            }
            this.acceptingCsrfPatterns = unmodifiableSet(acceptingCsrfPatterns);
        }
    }

    /**
     * Returns the configured rejecting CSRF URI patterns in the security configuration.
     *
     * @return rejecting CSRF URI patterns
     * @see #getAcceptingCsrfPatterns()
     */
    public Set<String> getRejectingCsrfPatterns() {
        return rejectingCsrfPatterns;
    }

    /**
     * Sets the rejecting CSRF patterns for the app.
     *
     * @param rejectingCsrfPatterns rejecting CSRF patterns to be set
     * @throws IllegalArgumentException if a pattern is null or empty
     */
    protected void setRejectingCsrfPatterns(Set<String> rejectingCsrfPatterns) {
        if (rejectingCsrfPatterns == null) {
            this.rejectingCsrfPatterns = emptySet();
        } else {
            for (String rejectingCsrfPattern : rejectingCsrfPatterns) {
                if (rejectingCsrfPattern == null) {
                    throw new IllegalArgumentException("A rejecting CSRF pattern cannot be null.");
                } else if (rejectingCsrfPattern.isEmpty()) {
                    throw new IllegalArgumentException("A rejecting CSRF pattern cannot be empty.");
                }
                // TODO: 12/31/16 check whether the 'rejectingCsrfPattern' is a valid URI pattern
            }
            this.rejectingCsrfPatterns = unmodifiableSet(rejectingCsrfPatterns);
        }
    }

    /**
     * Returns the configured accepting XSS URI patterns in the security configuration.
     *
     * @return accepting XSS URI patterns
     * @see #getRejectingXssPatterns()
     */
    public Set<String> getAcceptingXssPatterns() {
        return acceptingXssPatterns;
    }

    /**
     * Sets the accepting XSS patterns for the app.
     *
     * @param acceptingXssPatterns accepting XSS patterns to be set
     * @throws IllegalArgumentException if a pattern is null or empty
     */
    protected void setAcceptingXssPatterns(Set<String> acceptingXssPatterns) {
        if (acceptingXssPatterns == null) {
            this.acceptingXssPatterns = emptySet();
        } else {
            for (String acceptingXssPattern : acceptingXssPatterns) {
                if (acceptingXssPattern == null) {
                    throw new IllegalArgumentException("An accepting XSS pattern cannot be null.");
                } else if (acceptingXssPattern.isEmpty()) {
                    throw new IllegalArgumentException("An accepting XSS pattern cannot be empty.");
                }
                // TODO: 12/31/16 check whether the 'acceptingXssPattern' is a valid URI pattern
            }
            this.acceptingXssPatterns = unmodifiableSet(acceptingXssPatterns);
        }
    }

    /**
     * Returns the configured rejecting XSS URI patterns in the security configuration.
     *
     * @return rejecting XSS URI patterns
     * @see #getAcceptingXssPatterns()
     */
    public Set<String> getRejectingXssPatterns() {
        return rejectingXssPatterns;
    }

    /**
     * Sets the rejecting XSS patterns for the app.
     *
     * @param rejectingXssPatterns rejecting XSS patterns to be set
     * @throws IllegalArgumentException if a pattern is null or empty
     */
    protected void setRejectingXssPatterns(Set<String> rejectingXssPatterns) {
        if (rejectingXssPatterns == null) {
            this.rejectingXssPatterns = emptySet();
        } else {
            for (String rejectingXssPattern : rejectingXssPatterns) {
                if (rejectingXssPattern == null) {
                    throw new IllegalArgumentException("An rejecting XSS pattern cannot be null.");
                } else if (rejectingXssPattern.isEmpty()) {
                    throw new IllegalArgumentException("An rejecting XSS pattern cannot be empty.");
                }
                // TODO: 12/31/16 check whether the 'rejectingXssPattern' is a valid URI pattern
            }
            this.rejectingXssPatterns = unmodifiableSet(rejectingXssPatterns);
        }
    }

    /**
     * Returns the configured HTTP headers for the response in the security configuration.
     *
     * @return HTTP headers for the response
     */
    public Map<String, String> getResponseHeaders() {
        return responseHeaders;
    }

    protected void setResponseHeaders(Map<String, String> responseHeaders) {
        this.responseHeaders = (responseHeaders == null) ? emptyMap() : unmodifiableMap(responseHeaders);
    }

    /**
     * Return the business-logic related configurations that are configured for the app.
     *
     * @return business-logic related configurations
     */
    public Map<String, Object> other() {
        return otherConfigurations;
    }

    protected void setOther(Map<String, Object> other) {
        this.otherConfigurations = (other == null) ? emptyMap() : unmodifiableMap(other);
    }

    /**
     * Represents a configured menu item for an UUF App.
     *
     * @since 1.0.0
     */
    public static abstract class MenuItem {

        /**
         * Return the text of this menu item.
         *
         * @return text of this menu item
         */
        public abstract String getText();

        /**
         * Return the link of this menu item.
         *
         * @return link of this menu item
         */
        public abstract String getLink();

        /**
         * Return the icon of this menu item.
         *
         * @return icon of this menu item
         */
        public abstract String getIcon();

        /**
         * Return the sub-menus of this menu item.
         *
         * @return sub-menus of this menu item
         */
        public abstract List<? extends MenuItem> getSubmenus();
    }
}
