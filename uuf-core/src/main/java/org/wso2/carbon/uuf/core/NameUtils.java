package org.wso2.carbon.uuf.core;

public class NameUtils {

    public static String getFullyQualifiedName(String componentName, String name) {
        return isFullyQualifiedName(name) ? name : (componentName + "." + name);
    }

    public static String getSimpleName(String fullyQualifiedName) {
        // <component-name>.<binding/fragment-name>
        int lastDot = fullyQualifiedName.lastIndexOf('.');
        if (lastDot == -1) {
            throw new IllegalArgumentException("Name '" + fullyQualifiedName + "' is not a fully qualified name.");
        }
        return fullyQualifiedName.substring(lastDot + 1);
    }

    public static String getComponentName(String fullyQualifiedName) {
        // <component-name>.<binding/fragment-name>
        int lastDot = fullyQualifiedName.lastIndexOf('.');
        if (lastDot == -1) {
            throw new IllegalArgumentException("Name '" + fullyQualifiedName + "' is not a fully qualified name.");
        }
        return fullyQualifiedName.substring(0, lastDot);
    }

    public static boolean isFullyQualifiedName(String name) {
        return (name.lastIndexOf('.') != -1);
    }
}
