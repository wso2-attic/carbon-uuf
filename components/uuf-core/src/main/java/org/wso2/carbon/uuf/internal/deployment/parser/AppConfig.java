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
 *
 * Getters and setters of this class should match with getters and setters in
 * org.wso2.carbon.uuf.maven.bean.AppConfig class.
 * @since 1.0.0
 */
public class AppConfig {

    private String contextPath;
    private String theme;
    private String loginPageUri;
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

        private PatternsConfig csrfPatterns = new PatternsConfig();
        private PatternsConfig xssPatterns = new PatternsConfig();
        private Map<String, String> responseHeaders = Collections.emptyMap();

        /**
         * Returns CSRF URI patterns of this security configuration.
         *
         * @return CSRF URI patterns
         */
        public PatternsConfig getCsrfPatterns() {
            return csrfPatterns;
        }

        /**
         * Sets the CSRF URI patterns of this security configuration.
         *
         * @param csrfPatterns CSRF URI patterns to be set
         */
        public void setCsrfPatterns(PatternsConfig csrfPatterns) {
            this.csrfPatterns = (csrfPatterns == null) ? new PatternsConfig() : csrfPatterns;
        }

        /**
         * Returns XSS URI patterns of this security configuration.
         *
         * @return XSS URI patterns
         */
        public PatternsConfig getXssPatterns() {
            return xssPatterns;
        }

        /**
         * Sets the XSS URI patterns of this security configuration.
         *
         * @param xssPatterns XSS URI patterns to be set
         */
        public void setXssPatterns(PatternsConfig xssPatterns) {
            this.xssPatterns = (xssPatterns == null) ? new PatternsConfig() : xssPatterns;
        }

        /**
         * Returns HTTP response headers of this security configuration.
         *
         * @return HTTP response headers
         */
        public Map<String, String> getResponseHeaders() {
            return responseHeaders;
        }

        /**
         * Sets the HTTP response headers of this security configuration.
         *
         * @param responseHeaders HTTP response headers to be set
         */
        public void setResponseHeaders(Map<String, String> responseHeaders) {
            this.responseHeaders = (responseHeaders == null) ? Collections.emptyMap() : responseHeaders;
        }
    }

    /**
     * Bean class that represents security related URI patterns configurations in the app's config file of an UUF App.
     *
     * @since 1.0.0
     */
    public static class PatternsConfig {

        private List<String> accept = Collections.emptyList();
        private List<String> reject = Collections.emptyList();

        /**
         * Returns allowing URI patterns of this URI pattern configuration.
         *
         * @return allowing URI patterns
         */
        public List<String> getAccept() {
            return accept;
        }

        /**
         * Sets the allowing URI patterns of this URI pattern configuration.
         *
         * @param accept allowing URI patterns to be set
         */
        public void setAccept(List<String> accept) {
            this.accept = (accept == null) ? Collections.emptyList() : accept;
        }

        /**
         * Returns denying URI patterns of this URI pattern configuration.
         *
         * @return denying URI patterns
         */
        public List<String> getReject() {
            return reject;
        }

        /**
         * Sets the denying URI patterns of this URI pattern configuration.
         *
         * @param reject denying URI patterns to be set
         */
        public void setReject(List<String> reject) {
            this.reject = (reject == null) ? Collections.emptyList() : reject;
        }
    }
}
