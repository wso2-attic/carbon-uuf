package org.wso2.carbon.uuf.core.create;

import org.apache.commons.lang3.tuple.Pair;
import org.wso2.carbon.uuf.core.Renderable;

import java.util.Map;
import java.util.Optional;
import java.util.Set;

public interface RenderableCreator {
    Optional<Renderable> createRenderable(FileReference fileReference, ClassLoader cl);

    Optional<Pair<Renderable, Map<String, ? extends Renderable>>>
    createRenderableWithBindings(FileReference pageReference, ClassLoader loader);

    Set<String> getSupportedFileExtensions();
}
