package org.wso2.carbon.uuf.core;

import io.netty.handler.codec.http.HttpRequest;
import io.netty.handler.codec.http.HttpResponse;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

import javax.ws.rs.core.Response;
import java.util.Collections;
import java.util.HashMap;
import java.util.List;
import java.util.Map;
import java.util.Optional;
import java.util.function.Function;
import java.util.stream.Collectors;

public class App {
    private static final Logger log = LoggerFactory.getLogger(App.class);
    private final String context;
    private final List<Page> pages;
    private final Map<String, Fragment> fragmentsMap;
    private final Map<String, Renderable> bindings;

    public App(String context, List<Page> pages, List<Fragment> fragments, Map<String, Renderable> bindings) {
        if (!context.startsWith("/")) {
            throw new IllegalArgumentException("app context must start with a '/'");
        }
        this.context = context;

        // We sort uri so that more wildcard-ed ones go to the bottom.
        Collections.sort(pages, (o1, o2) -> o1.getUriPatten().compareTo(o2.getUriPatten()));
        this.pages = pages;

        // Convert the list to maps since we want O(1) access by name.
        this.fragmentsMap = fragments.stream().collect(Collectors.toMap(Fragment::getName, Function.identity()));

        this.bindings = bindings;
    }

    public List<Page> getPages() {
        return pages;
    }

    public Map<String, Fragment> getFragmentsMap() {
        return fragmentsMap;
    }

    public Map<String, Renderable> getBindings() {
        return bindings;
    }

    public String serve(HttpRequest request, HttpResponse response) {
        String pageUri = request.getUri().substring(context.length());
        Optional<Page> servingPage = getPage(pageUri);
        if (!servingPage.isPresent()) {
            throw new UUFException("Requested page '" + pageUri + "' does not exists.", Response.Status.NOT_FOUND);
        }

        Page page = servingPage.get();
        if (log.isDebugEnabled()) {
            log.debug("Page '" + page.toString() + "' is serving.");
        }

        Map<String, Object> model = new HashMap<>();
        model.put("pageUri", pageUri);

        return page.serve(model, bindings, fragmentsMap);
    }

    public Optional<Page> getPage(String pageUri) {
        String relativeUri = pageUri.substring(context.length());
        for (Page p : pages) {
            if (p.getUriPatten().match(relativeUri)) {
                return Optional.of(p);
            }
        }
        return Optional.empty();
    }

    @Override
    public String toString() {
        return "{\"context\": \"" + context + "\"}";
    }
}
