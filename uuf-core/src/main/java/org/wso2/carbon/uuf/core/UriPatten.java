package org.wso2.carbon.uuf.core;


import javax.annotation.Nonnull;
import java.util.Arrays;
import java.util.regex.Pattern;
import java.util.stream.Collectors;

/**
 * Note: this class has a natural ordering that is inconsistent with equals.
 */
public class UriPatten implements Comparable<UriPatten> {
    private static final Pattern WILDCARD = Pattern.compile("\\{(\\w+)\\}");
    private Pattern patten;
    private final String pattenString;

    public UriPatten(String patten) {
        pattenString = patten;
        if (!patten.startsWith("/")) {
            throw new IllegalArgumentException("URI patten must start with a '/'");
        }
        String escaped = Arrays.stream(WILDCARD.split(patten)).map(Pattern::quote).collect(Collectors.joining("\\w+"));
        if (patten.endsWith("}")) {
            escaped = escaped + "\\w+";
        }
        this.patten = Pattern.compile(escaped);
    }

    @Override
    public int compareTo(@Nonnull UriPatten o) {
        String[] a = WILDCARD.split(pattenString);
        String[] b = WILDCARD.split(o.pattenString);
        for (int i = 0; i < Math.min(a.length, b.length); i++) {
            int aLen = a[i].length();
            int bLen = b[i].length();
            if (aLen != bLen) {
                return bLen - aLen;
            }
        }
        if (a.length == b.length) {
            return 0;
        } else {
            return b.length - a.length;
        }
    }

    public boolean match(String uri) {
        return patten.matcher(uri).matches();
    }

    @Override
    public String toString() {
        return "{\"patten\": \"" + pattenString + "\"}";
    }
}
