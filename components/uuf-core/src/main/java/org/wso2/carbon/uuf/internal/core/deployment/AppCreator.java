/*
 *  Copyright (c) 2016, WSO2 Inc. (http://www.wso2.org) All Rights Reserved.
 *
 *  WSO2 Inc. licenses this file to you under the Apache License,
 *  Version 2.0 (the "License"); you may not use this file except
 *  in compliance with the License.
 *  You may obtain a copy of the License at
 *
 *  http://www.apache.org/licenses/LICENSE-2.0
 *
 *  Unless required by applicable law or agreed to in writing,
 *  software distributed under the License is distributed on an
 *  "AS IS" BASIS, WITHOUT WARRANTIES OR CONDITIONS OF ANY
 *  KIND, either express or implied.  See the License for the
 *  specific language governing permissions and limitations
 *  under the License.
 */

package org.wso2.carbon.uuf.internal.core.deployment;

import com.google.common.collect.ArrayListMultimap;
import com.google.common.collect.HashMultimap;
import com.google.common.collect.ListMultimap;
import com.google.common.collect.SetMultimap;
import org.wso2.carbon.uuf.api.Placeholder;
import org.wso2.carbon.uuf.api.config.ComponentManifest;
import org.wso2.carbon.uuf.api.config.Configuration;
import org.wso2.carbon.uuf.api.config.DependencyNode;
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
import org.wso2.carbon.uuf.exception.InvalidTypeException;
import org.wso2.carbon.uuf.exception.MalformedConfigurationException;
import org.wso2.carbon.uuf.exception.UUFException;
import org.wso2.carbon.uuf.internal.core.auth.SessionRegistry;
import org.wso2.carbon.uuf.internal.core.deployment.parser.ComponentManifestParser;
import org.wso2.carbon.uuf.internal.core.deployment.parser.ConfigurationParser;
import org.wso2.carbon.uuf.internal.core.deployment.parser.DependencyTreeParser;
import org.wso2.carbon.uuf.internal.util.NameUtils;
import org.wso2.carbon.uuf.spi.RenderableCreator;
import org.yaml.snakeyaml.Yaml;

import java.util.ArrayList;
import java.util.HashMap;
import java.util.HashSet;
import java.util.List;
import java.util.Map;
import java.util.Optional;
import java.util.Set;
import java.util.SortedSet;
import java.util.TreeSet;
import java.util.stream.Collectors;

import static org.wso2.carbon.uuf.internal.util.NameUtils.getFullyQualifiedName;

public class AppCreator {

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
        DependencyNode rootNode = DependencyTreeParser.parse(appReference.getDependencyTree());
        // Parse configurations.
        Map<?, ?> rawConfiguration = ConfigurationParser.parse(appReference.getConfiguration());
        // Create Lookup.
        final Lookup lookup = new Lookup(getFlattenedDependencies(rootNode), new Configuration(rawConfiguration));

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
            Component component = createComponent(componentName, componentVersion, componentContextPath,
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

    private Component createComponent(String componentName, String componentVersion, String componentContextPath,
                                      ComponentReference componentReference, ClassLoader classLoader,
                                      Lookup lookup) {
        componentReference.getLayouts(supportedExtensions)
                .map(layoutReference -> createLayout(layoutReference, componentName))
                .forEach(lookup::add);
        componentReference.getFragments(supportedExtensions)
                .map((fragmentReference) -> createFragment(fragmentReference, componentName, classLoader))
                .forEach(lookup::add);

        componentReference.getManifest().ifPresent(componentManifestFile -> {
            ComponentManifest componentManifest = ComponentManifestParser.parse(componentManifestFile);
            addBindings(componentManifest.getBindings(), lookup, componentName);
            // TODO: Register APIs
        });

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

    private void addBindings(List<ComponentManifest.Binding> bindings, Lookup lookup, String componentName) {
        if ((bindings == null) || bindings.isEmpty()) {
            return;
        }

        for (ComponentManifest.Binding binding : bindings) {
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
        Map<?, ?> rawConfig;
        try {
            rawConfig = new Yaml().loadAs(themeReference.getThemeConfig().getContent(), Map.class);
        } catch (Exception e) {
            // Yaml.loadAs() throws an Exception
            throw new MalformedConfigurationException(
                    "Theme configuration '" + themeReference.getThemeConfig().getRelativePath() + "' is malformed.", e);
        }

        ListMultimap<String, String> config = ArrayListMultimap.create();
        for (Map.Entry<?, ?> entry : rawConfig.entrySet()) {
            if (!(entry.getKey() instanceof String)) {
                throw new InvalidTypeException(
                        "Theme configuration must be a Map<String, String[]>. Instead found a '" +
                                entry.getKey().getClass().getName() + "' key.");
            }
            String key = (String) entry.getKey();
            if (!(key.equals(Placeholder.css.name()) || key.equals(Placeholder.headJs.name()) ||
                    key.equals(Placeholder.js.name()))) {
                throw new IllegalArgumentException(
                        "Theme configuration must be a Map<String, String[]> where key has to be either '" +
                                Placeholder.css + "', '" + Placeholder.headJs + "', and '" + Placeholder.js +
                                "'. Instead found '" + key + "' key.");
            }

            if (!(entry.getValue() instanceof List)) {
                throw new InvalidTypeException(
                        "Theme configuration must be a Map<String, List<String>>. Instead found a '" +
                                entry.getKey().getClass().getName() + "' value.");
            } else {
                List<?> rawList = (List) entry.getValue();
                for (Object listValue : rawList) {
                    if ((listValue instanceof String)) {
                        config.put(key, (String) listValue);
                    } else {
                        throw new InvalidTypeException(
                                "Theme configuration must be a Map<String, List<String>>. Instead found a '" +
                                        entry.getKey().getClass().getName() + "' value.");
                    }
                }
            }

        }

        return new Theme(themeReference.getName(), config.get(Placeholder.css.name()),
                         config.get(Placeholder.headJs.name()), config.get(Placeholder.js.name()),
                         themeReference.getPath());
    }
}
