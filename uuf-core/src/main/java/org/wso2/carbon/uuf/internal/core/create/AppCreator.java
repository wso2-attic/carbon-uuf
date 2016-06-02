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
import com.google.common.collect.HashMultimap;
import com.google.common.collect.ListMultimap;
import com.google.common.collect.SetMultimap;
import org.apache.commons.lang3.tuple.Pair;
import org.wso2.carbon.uuf.api.Configuration;
import org.wso2.carbon.uuf.api.Placeholder;
import org.wso2.carbon.uuf.core.App;
import org.wso2.carbon.uuf.core.Component;
import org.wso2.carbon.uuf.core.ComponentLookup;
import org.wso2.carbon.uuf.core.Fragment;
import org.wso2.carbon.uuf.core.Layout;
import org.wso2.carbon.uuf.core.Page;
import org.wso2.carbon.uuf.core.Theme;
import org.wso2.carbon.uuf.exception.InvalidTypeException;
import org.wso2.carbon.uuf.exception.MalformedConfigurationException;
import org.wso2.carbon.uuf.exception.UUFException;
import org.wso2.carbon.uuf.internal.core.UriPatten;
import org.wso2.carbon.uuf.internal.core.auth.SessionRegistry;
import org.wso2.carbon.uuf.reference.AppReference;
import org.wso2.carbon.uuf.reference.ComponentReference;
import org.wso2.carbon.uuf.reference.FileReference;
import org.wso2.carbon.uuf.reference.FragmentReference;
import org.wso2.carbon.uuf.reference.LayoutReference;
import org.wso2.carbon.uuf.reference.PageReference;
import org.wso2.carbon.uuf.reference.ThemeReference;
import org.wso2.carbon.uuf.spi.RenderableCreator;
import org.yaml.snakeyaml.Yaml;

import java.util.ArrayList;
import java.util.Collections;
import java.util.Deque;
import java.util.HashMap;
import java.util.HashSet;
import java.util.LinkedList;
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

    private int countLevel(String line) {
        int indent = 0;
        for (int i = 0; i < line.length(); i++) {
            char c = line.charAt(i);
            if (c == '+' || c == ' ' || c == '\\' || c == '|') {
                indent++;
            } else {
                break;
            }
        }
        return (indent == 1) ? indent : (indent / 2);
    }

    public App createApp(AppReference appReference) {
        List<String> dependencyLines = appReference.getDependencies();
        int previousLevel = -1;
        Set<Component> componentSiblings = new HashSet<>();
        Set<Component> componentDependencies;
        Deque<Set<Component>> componentLevels = new LinkedList<>();
        Set<Component> components = new HashSet<>();
        String appName = null;

        for (int i = dependencyLines.size() - 1; i >= 0; i--) {
            String line = dependencyLines.get(i);
            int currentLevel = countLevel(line);
            int jump = currentLevel - previousLevel;
            if ((jump == 0) || (previousLevel < 0)) {
                componentDependencies = Collections.<Component>emptySet();
            } else if (jump < 0) {
                componentDependencies = componentSiblings;
                componentSiblings = componentLevels.poll();
                if (componentSiblings == null) {
                    componentSiblings = new HashSet<>();
                }
            } else { // jump > 0
                componentDependencies = Collections.<Component>emptySet();
                componentLevels.push(componentSiblings);
                componentSiblings = new HashSet<>();
                for (int j = 0; j < jump - 1; j++) {
                    componentLevels.push(new HashSet<>());
                }
            }

            Pair<String, String> componentNameAndVersion = getComponentNameAndVersion(line);
            String componentName = componentNameAndVersion.getLeft();
            String componentVersion = componentNameAndVersion.getRight();
            String componentSimpleName, componentContext;
            if (i == 0) {
                componentSimpleName = Component.ROOT_COMPONENT_NAME;
                componentContext = Component.ROOT_COMPONENT_CONTEXT;
                appName = componentName; // Name of the root component is the full app name.
            } else {
                componentSimpleName = getSimpleName(componentName);
                componentContext = "/" + componentSimpleName;
            }
            ComponentReference componentReference = appReference.getComponentReference(componentSimpleName);
            ClassLoader componentClassLoader = classLoaderProvider.getClassLoader(componentName, componentVersion,
                                                                                  componentReference);
            // TODO: 5/3/16 check whether this component is already created
            Component component = createComponent(componentName, componentVersion, componentContext, componentReference,
                                                  componentDependencies, componentClassLoader);

            componentSiblings.add(component);
            components.add(component);
            previousLevel = currentLevel;
        }

        Set<Theme> themes = appReference.getThemeReferences().map(this::createTheme).collect(Collectors.toSet());

        return new App(appName, components, themes, new SessionRegistry(appName));
    }

    private Component createComponent(String componentName, String componentVersion, String componentContext,
                                      ComponentReference componentReference, Set<Component> dependencies,
                                      ClassLoader classLoader) {

        ComponentLookup lookup = createComponentLookup(componentName, componentContext, componentReference,
                                                       dependencies, classLoader);
        SortedSet<Page> pages = componentReference
                .getPages(supportedExtensions)
                .parallel()
                .map(pageReference -> createPage(pageReference, classLoader, lookup))
                .collect(Collectors.toCollection(TreeSet::new));

        return new Component(componentName, componentVersion, pages, lookup);
    }

    private ComponentLookup createComponentLookup(String componentName, String componentContext,
                                                  ComponentReference componentReference, Set<Component> dependencies,
                                                  ClassLoader classLoader) {
        Set<Layout> layouts = componentReference
                .getLayouts(supportedExtensions)
                .parallel()
                .map(layoutReference -> createLayout(componentName, layoutReference))
                .collect(Collectors.toSet());

        Map<String, Fragment> fragments = componentReference
                .getFragments(supportedExtensions)
                .parallel()
                .map((fragmentReference) -> createFragment(componentName, fragmentReference, classLoader))
                .collect(Collectors.toMap(Fragment::getName, fragment -> fragment));

        Yaml yaml = new Yaml();
        SetMultimap<String, Fragment> bindings;
        try {
            @SuppressWarnings("unchecked")
            Map<Object, Object> bindingsConfig = componentReference
                    .getBindingsConfig()
                    .map(fileReference -> yaml.loadAs(fileReference.getContent(), Map.class))
                    .orElse(Collections.emptyMap());
            bindings = createBindings(bindingsConfig, fragments, dependencies);
        } catch (Exception e) {
            // Yaml.loadAs() throws an Exception
            throw new MalformedConfigurationException(
                    "Bindings configuration '" + componentReference.getBindingsConfig().get().getRelativePath() +
                            "' of component '" + getSimpleName(componentName) + "' is malformed.", e);
        }

        Configuration configurations;
        try {
            Map<?, ?> rawConfigurations = componentReference
                    .getConfigurations()
                    .map(fileReference -> yaml.loadAs(fileReference.getContent(), Map.class))
                    .orElse(new HashMap<>(0));
            configurations = new Configuration(rawConfigurations);
        } catch (Exception e) {
            // Yaml.loadAs() throws an Exception
            throw new MalformedConfigurationException(
                    "Configuration '" + componentReference.getConfigurations().get().getRelativePath() +
                            "' of component '" + getSimpleName(componentName) + "' is malformed.", e);
        }

        return new ComponentLookup(componentName, componentContext, layouts, new HashSet<>(fragments.values()),
                                   bindings, configurations, dependencies);
    }

    private Layout createLayout(String componentName, LayoutReference layoutReference) {
        RenderableCreator renderableCreator = getRenderableCreator(layoutReference.getRenderingFile());
        RenderableCreator.LayoutRenderableData lrd = renderableCreator.createLayoutRenderable(layoutReference);
        return new Layout(getFullyQualifiedName(componentName, layoutReference.getName()), lrd.getRenderable());
    }

    private Fragment createFragment(String componentName, FragmentReference fragmentReference,
                                    ClassLoader classLoader) {
        RenderableCreator renderableCreator = getRenderableCreator(fragmentReference.getRenderingFile());
        RenderableCreator.FragmentRenderableData frd = renderableCreator.createFragmentRenderable(fragmentReference,
                                                                                                  classLoader);
        String fragmentName = getFullyQualifiedName(componentName, fragmentReference.getName());
        return new Fragment(fragmentName, frd.getRenderable(), frd.isSecured());
    }

    private SetMultimap<String, Fragment> createBindings(Map<Object, Object> bindingsConfig,
                                                         Map<String, Fragment> fragments, Set<Component> dependencies) {
        SetMultimap<String, Fragment> bindings = HashMultimap.create();
        if (bindingsConfig.isEmpty()) {
            return bindings;
        }

        Map<String, Fragment> allFragments = new HashMap<>(fragments);
        for (Component dependency : dependencies) {
            allFragments.putAll(dependency.getAllFragments());
        }

        for (Map.Entry<Object, Object> entry : bindingsConfig.entrySet()) {
            if (!(entry.getKey() instanceof String)) {
                throw new InvalidTypeException(
                        "A key (zone name) in the bindings configuration must be a string. Instead found '" +
                                entry.getKey().getClass() + "'.");
            }
            String zoneName = (String) entry.getKey();
            if (entry.getValue() instanceof String) {
                String fragmentName = (String) entry.getValue();
                Fragment fragment = allFragments.get(fragmentName);
                if (fragment == null) {
                    throw new IllegalArgumentException(
                            "Fragment '" + fragmentName + "' does not exists in this component or its dependencies.");
                } else {
                    bindings.put(zoneName, fragment);
                }
            } else if (entry.getValue() instanceof ArrayList) {
                ArrayList fragmentsNames = (ArrayList) entry.getValue();
                for (Object fragmentName : fragmentsNames) {
                    if (!(fragmentName instanceof String)) {
                        throw new InvalidTypeException("An array of values (fragment names) in the bindings " +
                                                               "configuration must be a string array. Instead found '" +
                                                               fragmentName.getClass() + "'.");
                    }
                    Fragment fragment = allFragments.get(fragmentName);
                    if (fragment == null) {
                        throw new IllegalArgumentException("Fragment '" + fragmentName + "' does not exists in this " +
                                                                   "component or its dependencies.");
                    } else {
                        bindings.put(zoneName, fragment);
                    }
                }
            } else {
                throw new InvalidTypeException("A value (fragment name/s) in the bindings configuration must be " +
                                                       "either a string or a string array. Instead found '" +
                                                       entry.getValue().getClass() + "'");
            }
        }

        return bindings;
    }

    private Page createPage(PageReference pageReference, ClassLoader classLoader, ComponentLookup lookup) {
        RenderableCreator renderableCreator = getRenderableCreator(pageReference.getRenderingFile());
        RenderableCreator.PageRenderableData prd = renderableCreator.createPageRenderable(pageReference, classLoader);
        UriPatten uriPatten = new UriPatten(pageReference.getPathPattern());
        if (prd.getLayoutName().isPresent()) {
            // This page has a layout.
            String layoutName = prd.getLayoutName().get();
            Optional<Layout> layout = lookup.getLayout(layoutName);
            if (layout.isPresent()) {
                return new Page(uriPatten, prd.getRenderable(), prd.isSecured(), layout.get());
            } else {
                throw new IllegalArgumentException("Layout '" + layoutName + "' mentioned in page '" +
                                                           pageReference.getRenderingFile().getRelativePath() +
                                                           "' does not exists in component '" +
                                                           lookup.getComponentName() + "' or its dependencies.");
            }
        } else {
            // This page does not have a layout.
            return new Page(uriPatten, prd.getRenderable(), prd.isSecured());
        }
    }

    private Pair<String, String> getComponentNameAndVersion(String dependencyLine) {
        // FORMAT: <group ID>:<artifact ID>:<artifact type>:<artifact version><":compile" or end of line>
        String[] parts = dependencyLine.split(":");
        if ((parts.length != 4) && (parts.length != 5)) {
            throw new MalformedConfigurationException(
                    "Dependency line '" + dependencyLine + "' is incorrect. It must be in '" +
                            "<group ID>:<artifact ID>:<artifact type>:<artifact version><\":compile\" or end of line>" +
                            "' format.");
        }
        // component name = <artifact ID> (2nd part), component version = <artifact version> (4th part)
        return Pair.of(parts[1], parts[3]);
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
