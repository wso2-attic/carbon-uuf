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

package org.wso2.carbon.uuf.internal.core.create;

import com.google.common.collect.ArrayListMultimap;
import com.google.common.collect.ListMultimap;
import org.apache.commons.lang3.tuple.Pair;
import org.wso2.carbon.uuf.api.Placeholder;
import org.wso2.carbon.uuf.core.App;
import org.wso2.carbon.uuf.core.Component;
import org.wso2.carbon.uuf.core.Fragment;
import org.wso2.carbon.uuf.core.Layout;
import org.wso2.carbon.uuf.core.Lookup;
import org.wso2.carbon.uuf.core.Page;
import org.wso2.carbon.uuf.core.Theme;
import org.wso2.carbon.uuf.exception.InvalidTypeException;
import org.wso2.carbon.uuf.exception.MalformedConfigurationException;
import org.wso2.carbon.uuf.exception.UUFException;
import org.wso2.carbon.uuf.internal.core.UriPatten;
import org.wso2.carbon.uuf.internal.core.auth.SessionRegistry;
import org.wso2.carbon.uuf.internal.util.NameUtils;
import org.wso2.carbon.uuf.reference.AppReference;
import org.wso2.carbon.uuf.reference.ComponentReference;
import org.wso2.carbon.uuf.reference.FileReference;
import org.wso2.carbon.uuf.reference.FragmentReference;
import org.wso2.carbon.uuf.reference.LayoutReference;
import org.wso2.carbon.uuf.reference.PageReference;
import org.wso2.carbon.uuf.reference.ThemeReference;
import org.wso2.carbon.uuf.spi.RenderableCreator;
import org.yaml.snakeyaml.Yaml;

import java.util.Collections;
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
import static org.wso2.carbon.uuf.internal.util.NameUtils.getSimpleName;

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

    public App createApp(AppReference appReference) {
        DependencyTreeParser.Result result = DependencyTreeParser.parse(appReference.getDependencies());

        Lookup lookup = new Lookup(result.getFlattenedDependencies());
        List<Set<Pair<String, String>>> leveledDependencies = result.getLeveledDependencies();
        Map<String, Component> createdComponents = new HashMap<>();
        String appName = null;

        for (int i = (leveledDependencies.size() - 1); i >= 0; i--) {
            Set<Pair<String, String>> dependencies = leveledDependencies.get(i); // dependencies at level i
            for (Pair<String, String> componentNameVersion : dependencies) {
                String componentName = componentNameVersion.getLeft();
                if (createdComponents.containsKey(componentName)) {
                    continue; // Component 'componentName' is already created.
                }

                String componentVersion = componentNameVersion.getRight();
                String componentSimpleName, componentContext;
                if (i == 0) {
                    // This happens only once, because when (i == 0) then (dependencies.size() == 1).
                    componentSimpleName = Component.ROOT_COMPONENT_SIMPLE_NAME;
                    componentContext = Component.ROOT_COMPONENT_CONTEXT;
                    appName = componentName; // Name of the root component is the full app name.
                } else {
                    componentSimpleName = NameUtils.getSimpleName(componentName);
                    componentContext = "/" + componentSimpleName;
                }

                ComponentReference componentReference = appReference.getComponentReference(componentSimpleName);
                ClassLoader classLoader = classLoaderProvider.getClassLoader(componentName, componentVersion,
                                                                             componentReference);
                Component component = createComponent(componentName, componentVersion, componentContext,
                                                      componentReference, classLoader, lookup);
                lookup.add(component);
                createdComponents.put(componentName, component);
            }
        }

        Set<Theme> themes = appReference.getThemeReferences().map(this::createTheme).collect(Collectors.toSet());

        return new App(appName, lookup, themes, new SessionRegistry(appName));
    }

    private Component createComponent(String componentName, String componentVersion, String componentContext,
                                      ComponentReference componentReference, ClassLoader classLoader,
                                      Lookup lookup) {
        componentReference.getLayouts(supportedExtensions)
                .parallel()
                .map(layoutReference -> createLayout(layoutReference, componentName))
                .forEach(lookup::add);
        componentReference.getFragments(supportedExtensions)
                .parallel()
                .map((fragmentReference) -> createFragment(fragmentReference, componentName, classLoader))
                .forEach(lookup::add);

        Yaml yaml = new Yaml();
        try {
            Map<?, ?> bindingsConfig = componentReference
                    .getBindingsConfig()
                    .map(fileReference -> yaml.loadAs(fileReference.getContent(), Map.class))
                    .orElse(Collections.emptyMap());
            addBindings(bindingsConfig, lookup, componentName);
        } catch (Exception e) {
            // Yaml.loadAs() throws an Exception
            throw new MalformedConfigurationException(
                    "Bindings configuration '" + componentReference.getBindingsConfig().get().getRelativePath() +
                            "' of component '" + getSimpleName(componentName) + "' is malformed.", e);
        }

        try {
            Map<?, ?> rawConfigurations = componentReference
                    .getConfigurations()
                    .map(fileReference -> yaml.loadAs(fileReference.getContent(), Map.class))
                    .orElse(new HashMap<>(0));
            lookup.getConfiguration().merge(rawConfigurations);
        } catch (Exception e) {
            // Yaml.loadAs() throws an Exception
            throw new MalformedConfigurationException(
                    "Configuration '" + componentReference.getConfigurations().get().getRelativePath() +
                            "' of component '" + getSimpleName(componentName) + "' is malformed.", e);
        }

        SortedSet<Page> pages = componentReference
                .getPages(supportedExtensions)
                .parallel()
                .map(pageReference -> createPage(pageReference, componentName, lookup, classLoader))
                .collect(Collectors.toCollection(TreeSet::new));
        return new Component(componentName, componentVersion, componentContext, pages);
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

    private void addBindings(Map<?, ?> bindingsConfig, Lookup lookup, String componentName) {
        if (bindingsConfig.isEmpty()) {
            return;
        }

        for (Map.Entry<?, ?> entry : bindingsConfig.entrySet()) {
            if (!(entry.getKey() instanceof String)) {
                throw new InvalidTypeException(
                        "A key (zone name) in the bindings configuration must be a string. Instead found '" +
                                entry.getKey().getClass() + "'.");
            }
            String zoneName = (String) entry.getKey();
            if (NameUtils.isSimpleName(zoneName)) {
                zoneName = NameUtils.getFullyQualifiedName(componentName, zoneName);
            }

            if (entry.getValue() instanceof String) {
                String fragmentName = (String) entry.getValue();
                Optional<Fragment> fragment = lookup.getFragmentIn(componentName, fragmentName);
                if (fragment.isPresent()) {
                    lookup.addBinding(zoneName, fragment.get());
                } else {
                    throw new IllegalArgumentException(
                            "Fragment '" + fragmentName + "' does not exists in component '" + componentName +
                                    "' or its dependencies.");
                }
            } else if (entry.getValue() instanceof List) {
                List fragmentsNames = (List) entry.getValue();
                for (Object fragmentNameObj : fragmentsNames) {
                    if (!(fragmentNameObj instanceof String)) {
                        throw new InvalidTypeException(
                                "An array of values (fragment names) in the bindings configuration must be a string " +
                                        "array. Instead found '" + fragmentNameObj.getClass() + "'.");
                    }
                    String fragmentName = (String) fragmentNameObj;
                    Optional<Fragment> fragment = lookup.getFragmentIn(componentName, fragmentName);
                    if (fragment.isPresent()) {
                        lookup.addBinding(zoneName, fragment.get());
                    } else {
                        throw new IllegalArgumentException(
                                "Fragment '" + fragmentName + "' does not exists in component '" + componentName +
                                        "' or its dependencies.");
                    }
                }
            } else {
                throw new InvalidTypeException("A value (fragment name/s) in the bindings configuration must be " +
                                                       "either a string or a string array. Instead found '" +
                                                       entry.getValue().getClass() + "'");
            }
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
                         config.get(Placeholder.headJs.name()), config.get(Placeholder.js.name()));
    }
}
