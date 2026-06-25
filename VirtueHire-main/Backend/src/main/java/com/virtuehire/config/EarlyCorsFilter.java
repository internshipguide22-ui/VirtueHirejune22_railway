package com.virtuehire.config;

import jakarta.servlet.FilterChain;
import jakarta.servlet.ServletException;
import jakarta.servlet.http.HttpServletRequest;
import jakarta.servlet.http.HttpServletResponse;
import org.springframework.beans.factory.annotation.Value;
import org.springframework.core.Ordered;
import org.springframework.core.annotation.Order;
import org.springframework.stereotype.Component;
import org.springframework.web.cors.CorsConfiguration;
import org.springframework.web.filter.OncePerRequestFilter;

import java.io.IOException;
import java.util.Arrays;

@Component
@Order(Ordered.HIGHEST_PRECEDENCE)
public class EarlyCorsFilter extends OncePerRequestFilter {

    private final CorsConfiguration corsConfiguration;

    public EarlyCorsFilter(
            @Value("${app.cors.allowed-origin-patterns:https://admin.virtuehire.in,https://*.up.railway.app,http://localhost:*}")
            String[] allowedOriginPatterns) {
        this.corsConfiguration = new CorsConfiguration();
        this.corsConfiguration.setAllowedOriginPatterns(CorsOriginPatterns.mergeWithRequiredPatterns(allowedOriginPatterns));
        this.corsConfiguration.setAllowedMethods(Arrays.asList("GET", "POST", "PUT", "PATCH", "DELETE", "OPTIONS"));
        this.corsConfiguration.setAllowedHeaders(Arrays.asList("*"));
        this.corsConfiguration.setExposedHeaders(Arrays.asList("Authorization", "Content-Disposition"));
        this.corsConfiguration.setAllowCredentials(true);
        this.corsConfiguration.setMaxAge(3600L);
    }

    @Override
    protected void doFilterInternal(HttpServletRequest request, HttpServletResponse response, FilterChain filterChain)
            throws ServletException, IOException {
        String origin = request.getHeader("Origin");
        String allowedOrigin = origin == null ? null : corsConfiguration.checkOrigin(origin);
        if (allowedOrigin == null && isRequiredOrigin(origin)) {
            allowedOrigin = origin;
        }

        if (allowedOrigin != null) {
            response.setHeader("Access-Control-Allow-Origin", allowedOrigin);
            response.setHeader("Access-Control-Allow-Credentials", "true");
            response.setHeader("Access-Control-Allow-Methods", "GET,POST,PUT,PATCH,DELETE,OPTIONS");
            response.setHeader("Access-Control-Allow-Headers", resolveAllowedHeaders(request));
            response.setHeader("Access-Control-Expose-Headers", "Authorization,Content-Disposition");
            response.setHeader("Access-Control-Max-Age", "3600");
            response.addHeader("Vary", "Origin");
            response.addHeader("Vary", "Access-Control-Request-Method");
            response.addHeader("Vary", "Access-Control-Request-Headers");
        }

        if ("OPTIONS".equalsIgnoreCase(request.getMethod())) {
            response.setStatus(HttpServletResponse.SC_OK);
            return;
        }

        filterChain.doFilter(request, response);
    }

    private String resolveAllowedHeaders(HttpServletRequest request) {
        String requestedHeaders = request.getHeader("Access-Control-Request-Headers");
        if (requestedHeaders == null || requestedHeaders.isBlank()) {
            return "*";
        }
        return requestedHeaders;
    }

    private boolean isRequiredOrigin(String origin) {
        return origin != null
                && ("https://admin.virtuehire.in".equals(origin)
                        || origin.endsWith(".up.railway.app")
                        || origin.startsWith("http://localhost:"));
    }
}
