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

import com.google.common.collect.HashMultimap;
import com.google.common.collect.SetMultimap;
import com.google.common.collect.Sets;
import org.apache.commons.lang3.math.NumberUtils;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import org.wso2.carbon.uuf.api.config.Configuration;
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
import org.wso2.carbon.uuf.core.Lookup;
import org.wso2.carbon.uuf.core.Page;
import org.wso2.carbon.uuf.core.Theme;
import org.wso2.carbon.uuf.core.UriPatten;
import org.wso2.carbon.uuf.exception.UUFException;
import org.wso2.carbon.uuf.internal.auth.SessionRegistry;
import org.wso2.carbon.uuf.internal.deployment.parser.YamlFileParser;
import org.wso2.carbon.uuf.internal.deployment.parser.bean.AppConfig;
import org.wso2.carbon.uuf.internal.deployment.parser.bean.ComponentConfig;
import org.wso2.carbon.uuf.internal.deployment.parser.bean.DependencyNode;
import org.wso2.carbon.uuf.internal.deployment.parser.bean.ThemeConfig;
import org.wso2.carbon.uuf.internal.util.NameUtils;
import org.wso2.carbon.uuf.spi.RenderableCreator;

import java.util.ArrayList;
import java.util.Collections;
import java.util.Dictionary;
import java.util.HashMap;
import java.util.HashSet;
import java.util.Hashtable;
import java.util.List;
import java.util.Map;
import java.util.Optional;
import java.util.Set;
import java.util.SortedSet;
import java.util.TreeSet;
import java.util.stream.Collectors;

import static org.wso2.carbon.uuf.internal.util.NameUtils.getFullyQualifiedName;

public class AppCreator {

    private static final Logger LOGGER = LoggerFactory.getLogger(AppCreator.class);
    private final Map<String, RenderableCreator> renderableCreators;
    private final Set<String> supportedExtensions;
    private final ClassLoaderProvider classLoaderProvider;

    public AppCreator(Set<RenderableCreator> renderableCreators, ClassLoaderProvider classLoaderProvider) {
        this.renderableCreators = new HashMap<>();
        this.supportedExtensions = new HashSet<>();
        for (RenderableCreator renderableCreator : renderableCreators) {
            for (String extension : renderableCreator.getSupportedFileExtensions()) {
                this.renderableCreators.put(extension, renderableCreator);
            }
            supportedExtensions.addAll(renderableCreator.getSupportedFileExtensions());
        }
        this.classLoaderProvider = classLoaderProvider;
    }

    public App createApp(AppReference appReference, String contextPath) {
        // Parse dependency tree.
        DependencyNode rootNode = YamlFileParser.parse(appReference.getDependencyTree(), DependencyNode.class);
        // Parse configurations.
        Configuration configuration = createConfiguration(appReference);
        // Create Lookup.
        final Lookup lookup = new Lookup(getFlattenedDependencies(rootNode), configuration);

        // Created Components.
        final Map<String, Component> createdComponents = new HashMap<>();
        rootNode.traverse(dependencyNode -> {
            if (createdComponents.containsKey(dependencyNode.getArtifactId())) {
                return; // Component for this dependency node is already created.
            }

            String componentName = dependencyNode.getArtifactId();
            String componentVersion = dependencyNode.getVersion();
            String componentContextPath = (dependencyNode == rootNode) ? Component.ROOT_COMPONENT_CONTEXT_PATH :
                    dependencyNode.getContextPath();
            ComponentReference componentReference = appReference.getComponentReference(componentContextPath);
            ClassLoader classLoader = classLoaderProvider.getClassLoader(componentName, componentVersion,
                                                                         componentReference);
            Component component = createComponent(contextPath, componentName, componentVersion, componentContextPath,
                                                  componentReference, classLoader, lookup);
            lookup.add(component);
            createdComponents.put(componentName, component);
        });
        // Create Themes.
        Set<Theme> themes = appReference.getThemeReferences().map(this::createTheme).collect(Collectors.toSet());
        // Create App.
        String appName = rootNode.getArtifactId();
        String appContextPath = (contextPath == null) ? rootNode.getContextPath() : contextPath;
        return new App(appName, appContextPath, lookup, themes, new SessionRegistry(appName));
    }

    private SetMultimap<String, String> getFlattenedDependencies(DependencyNode rootNode) {
        final SetMultimap<String, String> flattenedDependencies = HashMultimap.create();
        rootNode.traverse(dependencyNode -> {
            if (!flattenedDependencies.containsKey(dependencyNode.getArtifactId())) {
                flattenedDependencies.putAll(dependencyNode.getArtifactId(), dependencyNode.getAllDependencies());
            }
        });
        return flattenedDependencies;
    }

    private Configuration createConfiguration(AppReference appReference) {
        return new AppConfiguration(YamlFileParser.parse(appReference.getConfiguration(), AppConfig.class));
    }

    private Component createComponent(String appContextPath, String componentName, String componentVersion,
                                      String componentContextPath, ComponentReference componentReference,
                                      ClassLoader classLoader, Lookup lookup) {
        componentReference.getLayouts(supportedExtensions)
                .map(layoutReference -> createLayout(layoutReference, componentName))
                .forEach(lookup::add);
        componentReference.getFragments(supportedExtensions)
                .map((fragmentReference) -> createFragment(fragmentReference, componentName, classLoader))
                .forEach(lookup::add);

        ComponentConfig componentConfig = YamlFileParser.parse(componentReference.getConfiguration(),
                                                               ComponentConfig.class);
        addBindings(componentConfig.getBindings(), lookup, componentName);
        addAPIs(componentConfig.getApis(), appContextPath, componentContextPath, componentName, classLoader);

        if (!componentReference.getI18nFiles().isEmpty()) {
            lookup.add(componentReference.getI18nFiles());
        }

        SortedSet<Page> pages = componentReference.getPages(supportedExtensions)
                .map(pageReference -> createPage(pageReference, componentName, lookup, classLoader))
                .collect(Collectors.toCollection(TreeSet::new));

        return new Component(componentName, componentVersion, componentContextPath, pages,
                             componentReference.getPath());
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

    private void addBindings(List<ComponentConfig.Binding> bindings, Lookup lookup, String componentName) {
        if ((bindings == null) || bindings.isEmpty()) {
            return;
        }

        for (ComponentConfig.Binding binding : bindings) {
            String zoneName = NameUtils.getFullyQualifiedName(componentName, binding.getZoneName());
            List<Fragment> fragments = new ArrayList<>();
            for (String fragmentName : binding.getFragments()) {
                Optional<Fragment> fragment = lookup.getFragmentIn(componentName, fragmentName);
                if (fragment.isPresent()) {
                    fragments.add(fragment.get());
                } else {
                    throw new IllegalArgumentException("Fragment '" + fragmentName + "' does not exists in component '"
                                                               + componentName + "' or its dependencies.");
                }
            }
            lookup.addBinding(zoneName, fragments, binding.getMode());
        }
    }

    private void addAPIs(List<ComponentConfig.API> apis, String appContextPath, String componentContextPath,
                         String componentName, ClassLoader classLoader) {
        if ((apis == null) || apis.isEmpty()) {
            return;
        }

        for (ComponentConfig.API api : apis) {
            String className = api.getClassName();
            String uri = appContextPath + componentContextPath + "/apis" + api.getUri();
            Object apiImplementation;
            try {
                apiImplementation = classLoader.loadClass(className).newInstance();
            } catch (InstantiationException | IllegalAccessException | ClassNotFoundException e) {
                throw new UUFException(
                        "Cannot deploy REST API '" + className + "' for component '" + componentName + "'.", e);
            }
            Dictionary<String, String> serviceProperties = new Hashtable<>();
            serviceProperties.put("contextPath", uri);
            classLoaderProvider.deployAPI(apiImplementation, serviceProperties);
            LOGGER.info("Deployed REST API '{}' for component '{}' with context path '{}'.", className, componentName,
                        uri);
        }
    }

    private Page createPage(PageReference pageReference, String componentName, Lookup lookup, ClassLoader classLoader) {
        RenderableCreator renderableCreator = getRenderableCreator(pageReference.getRenderingFile());
        RenderableCreator.PageRenderableData prd = renderableCreator.createPageRenderable(pageReference, classLoader);
        UriPatten uriPatten = new UriPatten(pageReference.getPathPattern());
        if (prd.getLayoutName().isPresent()) {
            // This page has a layout.
            String layoutName = prd.getLayoutName().get();
            Optional<Layout> layout = lookup.getLayoutIn(componentName, layoutName);
            if (layout.isPresent()) {
                return new Page(uriPatten, prd.getRenderable(), prd.isSecured(), layout.get());
            } else {
                throw new IllegalArgumentException("Layout '" + layoutName + "' mentioned in page '" +
                                                           pageReference.getRenderingFile().getRelativePath() +
                                                           "' does not exists in component '" + componentName +
                                                           "' or its dependencies.");
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

    private static class AppConfiguration extends Configuration {

        public AppConfiguration(AppConfig appConfig) {
            setContextPath(appConfig.getContextPath());
            setThemeName(appConfig.getTheme());
            setLoginPageUri(appConfig.getLoginPageUri());
            Map<Integer, String> errorPageUris = appConfig.getErrorPages().entrySet().stream()
                    .filter(entry -> NumberUtils.isNumber(entry.getKey()))
                    .collect(Collectors.toMap(entry -> Integer.valueOf(entry.getKey()), Map.Entry::getValue));
            setErrorPageUris(errorPageUris);
            setDefaultErrorPageUri(appConfig.getErrorPages().get("default"));
            setMenus(appConfig.getMenus().stream()
                             .map(AppConfig.Menu::toConfigurationMenu)
                             .collect(Collectors.toList()));
            setAcceptingCsrfPatterns(Sets.newHashSet(appConfig.getSecurity().getCsrfPatterns().getAccept()));
            setRejectingCsrfPatterns(Sets.newHashSet(appConfig.getSecurity().getCsrfPatterns().getReject()));
            setAcceptingXssPatterns(Sets.newHashSet(appConfig.getSecurity().getXssPatterns().getAccept()));
            setRejectingXssPatterns(Sets.newHashSet(appConfig.getSecurity().getXssPatterns().getReject()));
            setResponseHeaders(appConfig.getSecurity().getResponseHeaders());
            setOther(appConfig.getOther());
        }
    }
}
