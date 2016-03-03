package org.wso2.carbon.uuf.core;


public class Fragment {

    private final String name;
    private final String template;

    public Fragment(String name, String template) {
        this.name = name;
        this.template = template;
    }

    public String getName() {
        return name;
    }
}
