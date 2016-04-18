package org.wso2.carbon.uuf.core.create;

import org.apache.commons.lang3.tuple.Pair;
import org.wso2.carbon.uuf.core.Renderable;

import java.util.Map;
import java.util.Set;

public interface RenderableCreator {
    Set<String> getSupportedFileExtensions();

    Renderable createFragmentRenderable(FragmentReference fragmentReference, ClassLoader classLoader);

    Pair<Renderable, Map<String, ? extends Renderable>> createPageRenderables(PageReference pageReference,
                                                                              ClassLoader classLoader);
}
