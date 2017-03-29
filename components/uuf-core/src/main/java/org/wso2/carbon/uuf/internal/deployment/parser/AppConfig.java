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

package org.wso2.carbon.uuf.internal.deployment.parser;

import org.wso2.carbon.uuf.api.config.Configuration;

import java.util.Collections;
import java.util.List;
import java.util.Map;
import java.util.stream.Collectors;

/**
 * Bean class that represents the app's config file of an UUF App.
 * <p>
 * Getters and setters of this class should match with getters and setters in org.wso2.carbon.uuf.maven.bean.AppConfig
 * class.
 *
 * @since 1.0.0
 */
public class AppConfig {

    private String contextPath;
    private String theme;
    private String loginPageUri;
    private String logoutPageUri;
    private String authenticator;
    private Map<String, String> errorPages = Collections.emptyMap();
    private List<Menu> menus = Collections.emptyList();
    private SecurityConfig security = new SecurityConfig();
    private Map<String, Object> otherConfigurations = Collections.emptyMap();

    /**
     * Returns the client-side context path in this app's config.
     *
     * @return client-side context path in this app's config.
     */
    public String getContextPath() {
        return contextPath;
    }

    /**
     * Sets the client-side context path in this app's config.
     *
     * @param contextPath client-side context path to be set
     */
    public void setContextPath(String contextPath) {
        this.contextPath = contextPath;
    }

    /**
     * Returns the theme name in this app's config.
     *
     * @return theme name in this app's config
     */
    public String getTheme() {
        return theme;
    }

    /**
     * Sets the theme name in this app's config.
     *
     * @param theme theme name to be set
     */
    public void setTheme(String theme) {
        this.theme = theme;
    }

    /**
     * Returns the login page URI in this app's config.
     *
     * @return URI of the login page in this app's config
     */
    public String getLoginPageUri() {
        return loginPageUri;
    }

    /**
     * Sets the login page URI in this app's config.
     *
     * @param loginPageUri URI of the login page to be set
     */
    public void setLoginPageUri(String loginPageUri) {
        this.loginPageUri = loginPageUri;
    }

    /**
     * Returns the logout page URI in this app's config.
     *
     * @return URI of the logout page in this app's config
     */
    public String getLogoutPageUri() {
        return logoutPageUri;
    }

    /**
     * Sets the logout page URI in this app's config. This logout page URI is not a mandatory field.
     *
     * @param logoutPageUri URI of the logout to be set
     */
    public void setLogoutPageUri(String logoutPageUri) {
        this.logoutPageUri = logoutPageUri;
    }

    /**
     * Returns the authenticator class in this app's config.
     *
     * @return Authenticator class name in this app's config
     */
    public String getAuthenticator() {
        return authenticator;
    }

    /**
     * Sets the authenticator class in this app's config.
     *
     * @param authenticator Authenticator class name to be set
     */
    public void setAuthenticator(String authenticator) {
        this.authenticator = authenticator;
    }

    /**
     * Returns the error pages URIs in this app's config.
     *
     * @return URIs of error pages in this app's config
     */
    public Map<String, String> getErrorPages() {
        return errorPages;
    }

    /**
     * Sets the error pages URIs in this app's config.
     *
     * @param errorPages URIs of error pages to be set
     */
    public void setErrorPages(Map<String, String> errorPages) {
        this.errorPages = (errorPages == null) ? Collections.emptyMap() : errorPages;
    }

    /**
     * Returns the menus in this app's config.
     *
     * @return menus in this app's config
     */
    public List<Menu> getMenus() {
        return menus;
    }

    /**
     * Sets the menus in this app's config.
     *
     * @param menus menus to be set
     */
    public void setMenus(List<Menu> menus) {
        this.menus = (menus == null) ? Collections.emptyList() : menus;
    }

    /**
     * Returns the security related configurations in this app's config.
     *
     * @return security related configurations in this app's config
     */
    public SecurityConfig getSecurity() {
        return security;
    }

    /**
     * Sets the security related configurations in this app's config.
     *
     * @param security security configs to be set
     */
    public void setSecurity(SecurityConfig security) {
        this.security = (security == null) ? new SecurityConfig() : security;
    }

    /**
     * Return the business-logic related configurations in this app's config.
     *
     * @return business-logic related configurations
     */
    public Map<String, Object> getOther() {
        return otherConfigurations;
    }

    /**
     * Sets the business-logic related configurations in this app's config.
     *
     * @param otherConfigurations business-logic related configurations to be set
     */
    public void setOther(Map<String, Object> otherConfigurations) {
        this.otherConfigurations = (otherConfigurations == null) ? Collections.emptyMap() : otherConfigurations;
    }

    /**
     * Bean class that represents a menu in the app's config file of an UUF App.
     *
     * @since 1.0.0
     */
    public static class Menu {

        private String name;
        private List<MenuItem> items;

        /**
         * Returns the name of this menu.
         *
         * @return text of this menu
         */
        public String getName() {
            return name;
        }

        /**
         * Sets the name of this menu.
         *
         * @param name name to be set
         */
        public void setName(String name) {
            this.name = name;
        }

        /**
         * Returns the menu items of this menu.
         *
         * @return menu items of this menu
         */
        public List<MenuItem> getItems() {
            return items;
        }

        /**
         * Sets the menu items of this menu.
         *
         * @param items menu items to be set
         */
        public void setItems(List<MenuItem> items) {
            this.items = items;
        }

        public Configuration.Menu toConfigurationMenu() {
            List<Configuration.MenuItem> menuItems = null;
            if (this.items != null) {
                menuItems = this.items.stream().map(MenuItem::toConfigurationMenuItem).collect(Collectors.toList());
            }
            return new Configuration.Menu(this.name, menuItems);
        }
    }

    /**
     * Bean class that represents a menu item in the app's config file of an UUF Component.
     *
     * @since 1.0.0
     */
    public static class MenuItem {

        private String text;
        private String link;
        private String icon;
        private List<MenuItem> submenus = Collections.emptyList();

        /**
         * Returns the text of this menu item.
         *
         * @return text of this menu item
         */
        public String getText() {
            return text;
        }

        /**
         * Sets the text of this menu item.
         *
         * @param text text to be set
         */
        public void setText(String text) {
            this.text = text;
        }

        /**
         * Returns the link of this menu item.
         *
         * @return link of this menu item.
         */
        public String getLink() {
            return link;
        }

        /**
         * Sets the link of this menu item.
         *
         * @param link link to be set
         */
        public void setLink(String link) {
            this.link = link;
        }

        /**
         * Returns the icon CSS class of this menu item.
         *
         * @return icon CSS class of this menu item
         */
        public String getIcon() {
            return icon;
        }

        /**
         * Sets the icon CSS class of this menu item.
         *
         * @param icon icon CSS class to be set
         */
        public void setIcon(String icon) {
            this.icon = icon;
        }

        /**
         * Returns the sub-menus of this menu item.
         *
         * @return sub-menus of this menu item
         */
        public List<MenuItem> getSubmenus() {
            return submenus;
        }

        /**
         * Sets the sub-menus of this menu item.
         *
         * @param submenus sub-menus to be set
         */
        public void setSubmenus(List<MenuItem> submenus) {
            this.submenus = submenus;
        }

        public Configuration.MenuItem toConfigurationMenuItem() {
            List<Configuration.MenuItem> subMenus = null;
            if (this.submenus != null) {
                subMenus = this.submenus.stream().map(MenuItem::toConfigurationMenuItem).collect(Collectors.toList());
            }
            return new Configuration.MenuItem(this.text, this.link, this.icon, subMenus);
        }
    }

    /**
     * Bean class that represents security related configurations in the app's config file of an UUF App.
     *
     * @since 1.0.0
     */
    public static class SecurityConfig {

        private List<String> csrfIgnoreUris = Collections.emptyList();
        private List<String> xssIgnoreUris = Collections.emptyList();
        private ResponseHeaders responseHeaders =  new ResponseHeaders();

        /**
         * Returns the list of URI's that doesn't require CSRF protection.
         *
         * @return list of URI's that doesn't require CSRF protection.
         */
        public List<String> getCsrfIgnoreUris() {
            return csrfIgnoreUris;
        }

        /**
         * Sets the list of URI's that doesn't require CSRF protection.
         *
         * @param csrfIgnoreUris list of URI's that doesn't require CSRF protection.
         */
        public void setCsrfIgnoreUris(List<String> csrfIgnoreUris) {
            this.csrfIgnoreUris = (csrfIgnoreUris == null) ? Collections.emptyList() : csrfIgnoreUris;
        }

        /**
         * Returns the list of URI's that doesn't require XSS protection.
         *
         * @return list of URI's that doesn't require XSS protection.
         */
        public List<String> getXssIgnoreUris() {
            return xssIgnoreUris;
        }

        /**
         * Sets the list of URI's that doesn't require XSS protection.
         *
         * @param xssIgnoreUris list of URI's that doesn't require XSS protection.
         */
        public void setXssIgnoreUris(List<String> xssIgnoreUris) {
            this.xssIgnoreUris = (xssIgnoreUris == null) ? Collections.emptyList() : xssIgnoreUris;
        }

        /**
         * Returns HTTP response headers of this security configuration.
         *
         * @return HTTP response headers
         */
        public ResponseHeaders getResponseHeaders() {
            return responseHeaders;
        }

        /**
         * Sets the HTTP response headers of this security configuration.
         *
         * @param responseHeaders HTTP response headers to be set
         */
        public void setResponseHeaders(ResponseHeaders responseHeaders) {
            this.responseHeaders = (responseHeaders == null) ? new ResponseHeaders() : responseHeaders;
        }
    }

    /**
     * Bean class that represents security headers configurations in the app's config file of an UUF App.
     *
     * @since 1.0.0
     */
    public static class ResponseHeaders {

        private Map<String, String> staticResources = Collections.emptyMap();
        private Map<String, String> pages = Collections.emptyMap();

        /**
         * Returns HTTP response headers for static contents.
         *
         * @return HTTP response headers
         */
        public Map<String, String> getStaticResources() {
            return staticResources;
        }

        /**
         * Sets the HTTP response headers for static contents.
         *
         * @param staticResources HTTP response headers to be set
         */
        public void setStaticResources(Map<String, String> staticResources) {
            this.staticResources = staticResources;
        }

        /**
         * Returns HTTP response headers for pages.
         *
         * @return HTTP response headers
         */
        public Map<String, String> getPages() {
            return pages;
        }

        /**
         * Sets the HTTP response headers for pages.
         *
         * @param pages HTTP response headers to be set
         */
        public void setPages(Map<String, String> pages) {
            this.pages = pages;
        }

        public Configuration.ResponseHeaders toConfigurationResponseHeaders() {
            return new Configuration.ResponseHeaders(staticResources, pages);
        }
    }
}
