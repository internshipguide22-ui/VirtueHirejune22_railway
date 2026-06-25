package com.virtuehire.config;

import java.util.ArrayList;
import java.util.Arrays;
import java.util.LinkedHashSet;
import java.util.List;
import java.util.Set;

final class CorsOriginPatterns {

    private static final List<String> REQUIRED_PATTERNS = List.of(
            "https://admin.virtuehire.in",
            "https://*.up.railway.app",
            "http://localhost:*");

    private CorsOriginPatterns() {
    }

    static List<String> mergeWithRequiredPatterns(String[] configuredPatterns) {
        Set<String> patterns = new LinkedHashSet<>(REQUIRED_PATTERNS);
        if (configuredPatterns != null) {
            Arrays.stream(configuredPatterns)
                    .filter(pattern -> pattern != null && !pattern.isBlank())
                    .flatMap(pattern -> Arrays.stream(pattern.split(",")))
                    .map(String::trim)
                    .filter(pattern -> !pattern.isBlank())
                    .forEach(patterns::add);
        }
        return new ArrayList<>(patterns);
    }
}
