package org.wso2.carbon.uuf.core.create;

import org.apache.commons.lang3.tuple.Pair;
import org.wso2.carbon.uuf.core.Renderable;

import java.util.Optional;
import java.util.Set;

public interface RenderableCreator {
    Set<String> getSupportedFileExtensions();

    Renderable createFragmentRenderable(FragmentReference fragmentReference, ClassLoader classLoader);

    Pair<Renderable, Optional<String>> createPageRenderable(PageReference pageReference, ClassLoader classLoader);

    Renderable createLayoutRenderable(LayoutReference layoutReference);

    int hashCode();

    boolean equals(Object obj);
}
