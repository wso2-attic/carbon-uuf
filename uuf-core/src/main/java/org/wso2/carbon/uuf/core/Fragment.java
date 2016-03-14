package org.wso2.carbon.uuf.core;

import javax.annotation.Nonnull;

/**
 *
 */
public class Fragment implements Comparable<Fragment> {

    private final String name;
    private final String path;
    private int index = Integer.MAX_VALUE;
    private final Renderable renderer;

    public Fragment(String name, String path, Renderable renderer) {
        this.name = name;
        this.path = path;
        this.renderer = renderer;
    }

    public String getName() {
        return name;
    }

    public String getPath() {
        return path;
    }

    public int getIndex() {
        return index;
    }

    public void setIndex(int index) {
        this.index = index;
    }


    public Renderable getRenderer() {
        return renderer;
    }

    @Override
    public int compareTo(@Nonnull Fragment other) {
        int deltaOfIndexes = (this.index - other.index);
        return (deltaOfIndexes < 0) ? +1 : ((deltaOfIndexes > 0) ? -1 : 0);
    }

    @Override
    public int hashCode() {
        return name.hashCode();
    }

    @Override
    public boolean equals(Object obj) {
        return (obj != null) && (obj instanceof Fragment) && (this.name.equals(((Fragment) obj).name));
    }

    @Override
    public String toString() {
        return "{\"name\": \"" + name + "\", \"path\": \"" + path + "\", \"index\": \"" + String.valueOf(index) +
                "\", \"renderer\": \"" + renderer.toString() + "\"}";
    }
}
