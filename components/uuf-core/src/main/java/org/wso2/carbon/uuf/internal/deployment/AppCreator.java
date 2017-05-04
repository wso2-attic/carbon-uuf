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

package org.wso2.carbon.uuf.internal.deployment;

import com.google.common.collect.Sets;
import org.apache.commons.lang3.math.NumberUtils;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import org.wso2.carbon.uuf.api.RestApi;
import org.wso2.carbon.uuf.api.config.Bindings;
import org.wso2.carbon.uuf.api.config.Configuration;
import org.wso2.carbon.uuf.api.config.I18nResources;
import org.wso2.carbon.uuf.api.reference.AppReference;
import org.wso2.carbon.uuf.api.reference.ComponentReference;
import org.wso2.carbon.uuf.api.reference.FileReference;
import org.wso2.carbon.uuf.api.reference.FragmentReference;
import org.wso2.carbon.uuf.api.reference.LayoutReference;
import org.wso2.carbon.uuf.api.reference.PageReference;
import org.wso2.carbon.uuf.api.reference.ThemeReference;
import org.wso2.carbon.uuf.core.App;
import org.wso2.carbon.uuf.core.Component;
import org.wso2.carbon.uuf.core.Fragment;
import org.wso2.carbon.uuf.core.Layout;
import org.wso2.carbon.uuf.core.Page;
import org.wso2.carbon.uuf.core.Theme;
import org.wso2.carbon.uuf.core.UriPatten;
import org.wso2.carbon.uuf.exception.ConfigurationException;
import org.wso2.carbon.uuf.exception.UUFException;
import org.wso2.carbon.uuf.internal.deployment.parser.AppConfig;
import org.wso2.carbon.uuf.internal.deployment.parser.ComponentConfig;
import org.wso2.carbon.uuf.internal.deployment.parser.DependencyNode;
import org.wso2.carbon.uuf.internal.deployment.parser.PropertyFileParser;
import org.wso2.carbon.uuf.internal.deployment.parser.ThemeConfig;
import org.wso2.carbon.uuf.internal.deployment.parser.YamlFileParser;
import org.wso2.carbon.uuf.internal.util.NameUtils;
import org.wso2.carbon.uuf.spi.RenderableCreator;

import java.util.ArrayList;
import java.util.Collections;
import java.util.HashMap;
import java.util.HashSet;
import java.util.List;
import java.util.Locale;
import java.util.Map;
import java.util.Set;
import java.util.SortedSet;
import java.util.TreeSet;

import static java.util.stream.Collectors.joining;
import static java.util.stream.Collectors.toCollection;
import static java.util.stream.Collectors.toList;
import static java.util.stream.Collectors.toMap;
import static java.util.stream.Collectors.toSet;
import static org.wso2.carbon.uuf.internal.util.NameUtils.getFullyQualifiedName;

public class AppCreator {

    private static final Logger LOGGER = LoggerFactory.getLogger(AppCreator.class);

    private final Map<String, RenderableCreator> renderableCreators;
    private final Set<String> supportedExtensions;
    private final ClassLoaderProvider classLoaderProvider;
    private final PluginProvider pluginProvider;
    private final RestApiDeployer restApiDeployer;

    public AppCreator(Set<RenderableCreator> renderableCreators, ClassLoaderProvider classLoaderProvider,
                      PluginProvider pluginProvider, RestApiDeployer restApiDeployer) {
        this.renderableCreators = new HashMap<>();
        this.supportedExtensions = new HashSet<>();
        for (RenderableCreator renderableCreator : renderableCreators) {
            for (String extension : renderableCreator.getSupportedFileExtensions()) {
                this.renderableCreators.put(extension, renderableCreator);
            }
            supportedExtensions.addAll(renderableCreator.getSupportedFileExtensions());
        }
        this.classLoaderProvider = classLoaderProvider;
        this.pluginProvider = pluginProvider;
        this.restApiDeployer = restApiDeployer;
    }

    public App createApp(AppReference appReference, String contextPath) {
        // Parse dependency tree.
        DependencyNode rootNode = YamlFileParser.parse(appReference.getDependencyTree(), DependencyNode.class);

        final String appName = rootNode.getArtifactId();
        final String appContextPath = (contextPath == null) ? rootNode.getContextPath() : contextPath;
        final Configuration configuration = createConfiguration(appReference);
        final Bindings bindings = new Bindings();
        final I18nResources i18nResources = new I18nResources();

        // Create components.
        final Map<String, Component> createdComponents = new HashMap<>();
        rootNode.traverse(dependencyNode -> {
            if (createdComponents.containsKey(dependencyNode.getArtifactId())) {
                return; // Component for this dependency node is already created.
            }

            Component component = createComponent(dependencyNode, appReference, rootNode, appContextPath,
                                                  createdComponents, bindings, i18nResources);
            createdComponents.put(component.getName(), component);
        });

        // Create Themes.
        final Set<Theme> themes = appReference.getThemeReferences().map(this::createTheme).collect(toSet());

        // Create App.
        return new App(appName, appContextPath, new HashSet<>(createdComponents.values()), themes, configuration,
                       bindings, i18nResources);
    }

    private Configuration createConfiguration(AppReference appReference) {
        AppConfig appConfig = YamlFileParser.parse(appReference.getConfiguration(), AppConfig.class);
        Configuration configuration = new Configuration();
        configuration.setContextPath(appConfig.getContextPath());
        configuration.setThemeName(appConfig.getTheme());
        configuration.setLoginPageUri(appConfig.getLoginPageUri());
        Map<Integer, String> errorPageUris = appConfig.getErrorPages().entrySet().stream()
                .filter(entry -> NumberUtils.isNumber(entry.getKey()))
                .collect(toMap(entry -> Integer.valueOf(entry.getKey()), Map.Entry::getValue));
        configuration.setErrorPageUris(errorPageUris);
        configuration.setDefaultErrorPageUri(appConfig.getErrorPages().get("default"));
        configuration.setMenus(appConfig.getMenus().stream()
                                       .map(AppConfig.Menu::toConfigurationMenu)
                                       .collect(toList()));
        configuration.setCsrfIgnoreUris(Sets.newHashSet(appConfig.getSecurity().getCsrfIgnoreUris()));
        configuration.setXssIgnoreUris(Sets.newHashSet(appConfig.getSecurity().getXssIgnoreUris()));
        configuration.setResponseHeaders(appConfig.getSecurity().getResponseHeaders().toConfigurationResponseHeaders());
        configuration.setOther(appConfig.getOther());
        return configuration;
    }

    private Component createComponent(DependencyNode componentNode, AppReference appReference,
                                      DependencyNode rootNode, String appContextPath,
                                      Map<String, Component> createdComponents, Bindings bindings,
                                      I18nResources i18nResources) {
        final String componentName = componentNode.getArtifactId();
        final String componentVersion = componentNode.getVersion();
        final String componentContextPath =
                (componentNode == rootNode) ? Component.ROOT_COMPONENT_CONTEXT_PATH : componentNode.getContextPath();
        ComponentReference componentReference = appReference.getComponentReference(componentContextPath);
        ClassLoader classLoader = classLoaderProvider.getClassLoader(componentName, componentVersion,
                                                                     componentReference);

        // Dependency components.
        final Set<Component> dependencies = componentNode.getDependencies().stream()
                .map(dependencyNode -> createdComponents.get(dependencyNode.getArtifactId()))
                .collect(toSet());
        // Create layouts in the component.
        final Set<Layout> layouts = componentReference.getLayouts(supportedExtensions)
                .map(layoutReference -> createLayout(layoutReference, componentName))
                .collect(toSet());
        // Create pages in the component.
        final Set<Fragment> fragments = componentReference.getFragments(supportedExtensions)
                .map(fragmentReference -> createFragment(fragmentReference, componentName, classLoader))
                .collect(toSet());
        // Create pages in the component.
        Map<String, Layout> availableLayouts = new HashMap<>();
        layouts.forEach(layout -> availableLayouts.put(layout.getName(), layout));
        dependencies.forEach(cmp -> cmp.getLayouts().forEach(l -> availableLayouts.put(l.getName(), l)));
        final SortedSet<Page> pages = componentReference.getPages(supportedExtensions)
                .map(pageReference -> createPage(pageReference, classLoader, availableLayouts, componentName))
                .collect(toCollection(TreeSet::new));

        // Handle component's configurations.
        ComponentConfig componentConfig = YamlFileParser.parse(componentReference.getConfiguration(),
                                                               ComponentConfig.class);
        addBindings(componentConfig.getBindings(), bindings, componentName, fragments, dependencies);
        addRestApis(componentConfig.getApis(), appContextPath, componentContextPath, classLoader);

        componentReference.getI18nFiles().forEach(i18nFile -> {
            Locale locale = Locale.forLanguageTag(i18nFile.getNameWithoutExtension());
            if (locale.getLanguage().isEmpty()) {
                throw new ConfigurationException(
                        "Cannot identify the locale of the language file '" + i18nFile.getAbsolutePath() +
                                "' of component '" + componentName + "'.");
            }
            i18nResources.addI18nResource(locale, PropertyFileParser.parse(i18nFile));
        });

        return new Component(componentName, componentVersion, componentContextPath, pages, fragments, layouts,
                             dependencies, componentReference.getPath());
    }

    private Layout createLayout(LayoutReference layoutReference, String componentName) {
        RenderableCreator renderableCreator = getRenderableCreator(layoutReference.getRenderingFile());
        RenderableCreator.LayoutRenderableData lrd = renderableCreator.createLayoutRenderable(layoutReference);
        return new Layout(getFullyQualifiedName(componentName, layoutReference.getName()), lrd.getRenderable());
    }

    private Fragment createFragment(FragmentReference fragmentReference, String componentName,
                                    ClassLoader classLoader) {
        RenderableCreator renderableCreator = getRenderableCreator(fragmentReference.getRenderingFile());
        RenderableCreator.FragmentRenderableData frd = renderableCreator.createFragmentRenderable(fragmentReference,
                                                                                                  classLoader);
        String fragmentName = getFullyQualifiedName(componentName, fragmentReference.getName());
        return new Fragment(fragmentName, frd.getRenderable(), frd.isSecured());
    }

    private void addBindings(List<ComponentConfig.Binding> bindingEntries, Bindings bindings, String componentName,
                             Set<Fragment> componentFragments, Set<Component> componentDependencies) {
        if (bindingEntries == null || bindingEntries.isEmpty()) {
            return;
        }

        Map<String, Fragment> availableFragments = new HashMap<>();
        componentFragments.forEach(fragment -> availableFragments.put(fragment.getName(), fragment));
        componentDependencies.forEach(cmp -> cmp.getFragments().forEach(f -> availableFragments.put(f.getName(), f)));

        for (ComponentConfig.Binding entry : bindingEntries) {
            if (entry.getZoneName() == null) {
                throw new ConfigurationException(
                        "Zone name of a binding entry cannot be null. Found such binding entry in component '" +
                                componentName + "'.");
            } else if (entry.getZoneName().isEmpty()) {
                throw new ConfigurationException(
                        "Zone name of a binding entry cannot be empty. Found such binding entry in component '" +
                                componentName + "'.");
            }
            String zoneName = NameUtils.getFullyQualifiedName(componentName, entry.getZoneName());

            if (entry.getFragments() == null) {
                throw new ConfigurationException(
                        "Fragments in a binding entry cannot be null. Found such binding entry in component '" +
                                componentName + "'.");
            }
            List<Fragment> fragments = new ArrayList<>(entry.getFragments().size());
            for (String fragmentName : entry.getFragments()) {
                Fragment fragment = availableFragments.get(NameUtils.getFullyQualifiedName(componentName,
                                                                                           fragmentName));
                if (fragment == null) {
                    throw new IllegalArgumentException(
                            "Fragment '" + fragmentName + "' given in the binding entry '" + entry +
                                    "' does not exists in component '" + componentName + "' or its dependencies " +
                                    componentDependencies.stream().map(Component::getName).collect(joining(",")) + ".");
                }
                fragments.add(fragment);
            }
            bindings.addBinding(zoneName, fragments, entry.getMode());
        }
    }

    private void addRestApis(List<ComponentConfig.API> apis, String appContextPath, String componentContextPath,
                             ClassLoader classLoader) {
        if ((apis == null) || apis.isEmpty()) {
            return;
        }

        for (ComponentConfig.API api : apis) {
            String apiContextPath = appContextPath + componentContextPath + "/apis" + api.getUri();
            RestApi restApi = pluginProvider.getPluginInstance(RestApi.class, api.getClassName(), classLoader);
            restApiDeployer.deploy(restApi, apiContextPath);
        }
    }

    private Page createPage(PageReference pageReference, ClassLoader classLoader, Map<String, Layout> availableLayouts,
                            String componentName) {
        FileReference pageRenderingFile = pageReference.getRenderingFile();
        RenderableCreator renderableCreator = getRenderableCreator(pageRenderingFile);
        RenderableCreator.PageRenderableData prd = renderableCreator.createPageRenderable(pageReference, classLoader);
        UriPatten uriPatten = new UriPatten(pageReference.getPathPattern());
        if (prd.getLayoutName().isPresent()) {
            // This page has a layout.
            String layoutName = NameUtils.getFullyQualifiedName(componentName, prd.getLayoutName().get());
            Layout layout = availableLayouts.get(layoutName);
            if (layout != null) {
                return new Page(uriPatten, prd.getRenderable(), prd.isSecured(), layout);
            } else {
                throw new IllegalArgumentException(
                        "Layout '" + layoutName + "' used in page '" + pageRenderingFile.getRelativePath() +
                                "' does not exists in component '" + componentName + "' or its dependencies.");
            }
        } else {
            // This page does not have a layout.
            return new Page(uriPatten, prd.getRenderable(), prd.isSecured());
        }
    }

    private RenderableCreator getRenderableCreator(FileReference fileReference) {
        RenderableCreator renderableCreator = renderableCreators.get(fileReference.getExtension());
        if (renderableCreator == null) {
            throw new UUFException(
                    "Cannot find a RenderableCreator for file type '" + fileReference.getExtension() + "'.");
        }
        return renderableCreator;
    }

    private Theme createTheme(ThemeReference themeReference) {
        ThemeConfig themeConfig = YamlFileParser.parse(themeReference.getConfiguration(), ThemeConfig.class);
        List<String> css = (themeConfig.getCss() == null) ? Collections.emptyList() : themeConfig.getCss();
        List<String> headJs = (themeConfig.getHeadJs() == null) ? Collections.emptyList() : themeConfig.getHeadJs();
        List<String> js = (themeConfig.getJs() == null) ? Collections.emptyList() : themeConfig.getJs();

        return new Theme(themeReference.getName(), css, headJs, js, themeReference.getPath());
    }
}
