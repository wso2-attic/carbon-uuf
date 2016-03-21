package org.wso2.carbon.uuf.core;

import java.util.ArrayList;
import java.util.List;
import java.util.Map;
import java.util.Set;
import java.util.stream.Collectors;

public class Component {
    private final String name;
    private final String context;
    private final List<Page> pages;
    private final Map<String, Fragment> fragments;

    public Component(String name, String context, Set<Page> pages, Set<Fragment> fragments) {
        this.name = name;
        this.context = context;
        this.pages = new ArrayList<>(pages);
        this.fragments = fragments.stream().collect(Collectors.toMap(Fragment::getName, fragment -> fragment));
    }

    public String renderPage(String pageUri) {
        return null;
    }

    public String getName() {
        return name;
    }
}
