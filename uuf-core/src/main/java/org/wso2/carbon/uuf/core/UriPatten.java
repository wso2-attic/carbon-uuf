package org.wso2.carbon.uuf.core;

import org.apache.commons.lang3.tuple.Pair;

import java.util.ArrayList;
import java.util.HashMap;
import java.util.List;
import java.util.Map;
import java.util.regex.Matcher;
import java.util.regex.Pattern;
import java.util.stream.Collectors;

/**
 * Note: this class has a natural ordering that is inconsistent with equals.
 */
public class UriPatten implements Comparable<UriPatten> {

    private static final Pattern URI_VARIABLE_PATTERN = Pattern.compile("\\{(.+?)\\}");
    private static final String URI_VARIABLE_REGEX = "([^/]+)";
    private static final String PLUS_MARKED_URI_VARIABLE_REGEX = "(.+)";

    private final String patternString;
    private final Pattern pattern;
    private final List<String> variableNames;

    public UriPatten(String uriPattern) {
        Pair<Boolean, List<String>> analyseResult = analyse(uriPattern);
        this.patternString = uriPattern;
        boolean hasPlusMarkedVariable = analyseResult.getLeft();
        this.variableNames = analyseResult.getRight();

        String patternRegex = URI_VARIABLE_PATTERN.splitAsStream(uriPattern)
                .map(Pattern::quote)
                .collect(Collectors.joining(URI_VARIABLE_REGEX));
        if (patternRegex.endsWith("/index")) {
            patternRegex = patternRegex.substring(0, (patternRegex.length() - 5));
        } else if (uriPattern.charAt(uriPattern.length() - 1) == '}') {
            patternRegex += (hasPlusMarkedVariable) ? PLUS_MARKED_URI_VARIABLE_REGEX : URI_VARIABLE_REGEX;
        }
        pattern = Pattern.compile(patternRegex);
    }

    private Pair<Boolean, List<String>> analyse(String uriPattern) {
        if (uriPattern.isEmpty()) {
            throw new IllegalArgumentException("URI pattern cannot be empty.");
        }
        if (uriPattern.charAt(0) != '/') {
            throw new IllegalArgumentException("URI patten must start with a '/'.");
        }

        int delta = 0;
        int currentVariableStartIndex = -1;
        boolean hasPlusMarkedVariable = false;
        List<String> variableNames = new ArrayList<>();
        for (int i = 1; i < uriPattern.length(); i++) {
            char currentChar = uriPattern.charAt(i);
            if (currentChar == '{') {
                delta++;
                if (delta != 1) {
                    throw new IllegalArgumentException("Illegal URI variable opening '{' found at index " + i +
                                                               " in URI pattern '" + uriPattern +
                                                               "'. Cannot declare a variable inside another variable.");
                }
                currentVariableStartIndex = i + 1; // to doge the '{' char
            } else if (currentChar == '}') {
                delta--;
                if (delta != 0) {
                    throw new IllegalArgumentException("Illegal URI variable closing '}' found at index " + i +
                                                               " in of URI pattern '" + uriPattern +
                                                               "'. Cannot find matching opening.");
                }
                if (hasPlusMarkedVariable && (i != uriPattern.length() - 1)) {
                    throw new IllegalArgumentException(
                            "Illegal character found at index " + (i + 1) + " in URI pattern '" + uriPattern +
                                    "'. Cannot have any more characters after enclosing a 'one or more matching' type" +
                                    " URI variable declaration.");
                }
                variableNames.add(uriPattern.substring(currentVariableStartIndex, i));
            } else if (currentChar == '+') {
                if ((delta == 1) && (uriPattern.charAt(i - 1) == '{')) {
                    currentVariableStartIndex++; // to doge the '+' char
                    hasPlusMarkedVariable = true;
                }
            }
        }
        if (delta > 0) {
            throw new IllegalArgumentException("Illegal URI variable opening '{' found at index " +
                                                       (currentVariableStartIndex - 1) + " in URI pattern '" +
                                                       uriPattern + "' which was never closed.");
        }
        return Pair.of(hasPlusMarkedVariable, variableNames);
    }

    public boolean matches(String uri) {
        return pattern.matcher(uri).matches();
    }

    public Map<String, String> match(String uri) {
        Map<String, String> result = new HashMap<>(variableNames.size());
        Matcher matcher = this.pattern.matcher(uri);
        if (matcher.find()) {
            for (int i = 1; i <= matcher.groupCount(); i++) {
                String name = variableNames.get(i - 1);
                String value = matcher.group(i);
                result.put(name, value);
            }
        }
        return result;
    }

    @Override
    public int compareTo(UriPatten otherUriPattern) {
        if (otherUriPattern == null) {
            return 1;
        }

        String[] a = URI_VARIABLE_PATTERN.split(patternString);
        String[] b = URI_VARIABLE_PATTERN.split(otherUriPattern.patternString);
        for (int i = 0; i < Math.min(a.length, b.length); i++) {
            int aLen = a[i].length();
            int bLen = b[i].length();
            if (aLen != bLen) {
                return bLen - aLen;
            }
        }
        if (a.length == b.length) {
            return otherUriPattern.patternString.compareTo(patternString);
        } else {
            return b.length - a.length;
        }
    }

    @Override
    public String toString() {
        return "{\"pattern\": \"" + patternString + "\", \"regex\": \"" + pattern.pattern() + "\"}";
    }
}
