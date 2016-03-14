package org.wso2.carbon.uuf.core;

import io.netty.handler.codec.http.HttpRequest;
import io.netty.handler.codec.http.HttpResponse;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

import javax.ws.rs.core.Response;
import java.util.Collections;
import java.util.List;
import java.util.Map;
import java.util.Optional;
import java.util.function.Function;
import java.util.stream.Collectors;

public class App {
    private static final Logger log = LoggerFactory.getLogger(App.class);
    private final String context;
    private final List<Page> pages;
    private final List<Fragment> fragments;
    private final Map<String, Fragment> fragmentsMap;
    private final Map<String, Fragment> bindings;

    public App(String context, List<Page> pages, List<Fragment> fragments, Map<String, Fragment> bindings) {
        if (!context.startsWith("/")) {
            throw new IllegalArgumentException("app context must start with a '/'");
        }
        this.context = context;

        // We sort uri so that more wildcard-ed ones go to the bottom.
        Collections.sort(pages, (o1, o2) -> o1.getUriPatten().compareTo(o2.getUriPatten()));
        this.pages = pages;

        this.fragments = fragments;
        // Convert the list to maps since we want O(1) access by name.
        this.fragmentsMap = fragments.stream().collect(Collectors.toMap(Fragment::getName, Function.identity()));

        this.bindings = bindings;
    }

    public List<Page> getPages() {
        return pages;
    }

    public List<Fragment> getFragments() {
        return fragments;
    }

    public Map<String, Fragment> getFragmentsMap() {
        return fragmentsMap;
    }

    public Map<String, Fragment> getBindings() {
        return bindings;
    }

    public String serve(HttpRequest request, HttpResponse response) {
        Optional<Page> page = getPage(request.getUri());
        if (page.isPresent()) {
            if (log.isDebugEnabled()) {
                log.debug("page " + page + " is selected");
            }
            return page.get().serve(null, null, null);
        } else {
            throw new UUFException(
                    "No page by the URI '" + request.getUri() + "'",
                    Response.Status.NOT_FOUND);
        }
    }

    private Optional<Page> getPage(String uri) {
        if (!uri.startsWith(context)) {
            throw new IllegalArgumentException("Request for '" + uri + "' can't be served by " + this);
        }

        String relativeUri = uri.substring(context.length());
        for (Page p : pages) {
            if (p.getUriPatten().match(relativeUri)) {
                return Optional.of(p);
            }
        }
        return Optional.empty();
    }

    @Override
    public String toString() {
        return "App{context='" + context + "'}";
    }
}
