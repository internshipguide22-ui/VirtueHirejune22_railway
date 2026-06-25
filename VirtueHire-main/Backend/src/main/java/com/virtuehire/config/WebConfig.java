package com.virtuehire.config;

import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.beans.factory.annotation.Value;
import org.springframework.context.annotation.Bean;
import org.springframework.context.annotation.Configuration;
import org.springframework.web.cors.CorsConfiguration;
import org.springframework.web.cors.CorsConfigurationSource;
import org.springframework.web.cors.UrlBasedCorsConfigurationSource;
import org.springframework.web.servlet.config.annotation.CorsRegistry;
import org.springframework.web.servlet.config.annotation.InterceptorRegistry;
import org.springframework.web.servlet.config.annotation.WebMvcConfigurer;

import java.util.Arrays;

@Configuration
public class WebConfig implements WebMvcConfigurer {

    @Autowired
    private JwtInterceptor jwtInterceptor;

    @Value("${app.cors.allowed-origin-patterns:https://admin.virtuehire.in,https://*.up.railway.app,http://localhost:*}")
    private String[] allowedOriginPatterns;

    @Override
    public void addInterceptors(InterceptorRegistry registry) {
        // Register interceptor but exclude public endpoints and auth endpoints
        registry.addInterceptor(jwtInterceptor)
                .addPathPatterns("/api/**")
                .excludePathPatterns(
                        "/api/auth/**",
                        "/api/public/**",
                        "/api/hrs/login",
                        "/api/hrs/register",
                        "/api/hrs/verify-email",
                        "/api/hrs/resend-otp",
                        "/api/candidates/login",
                        "/api/candidates/register",
                        "/api/candidates/verify-otp",
                        "/api/candidates/verify-email",
                        "/api/candidates/resend-otp",
                        "/api/candidates/forgot-password",
                        "/api/candidates/reset-password",
                        "/api/candidates/resumes/draft/pdf",
                        "/api/candidates/file/**",

// ── BEFORE ────────────────────────────────────────
// "/api/candidates/me/resume" was NOT excluded here,
// so the JwtInterceptor blocked the request before it
// reached CandidateRestController, causing 401/404.
// Same problem for profile-pic and hrs/file endpoints.
// ── AFTER ─────────────────────────────────────────
// These session-based endpoints use HttpSession auth,
// NOT JWT — so they must bypass the JWT interceptor.
"/api/candidates/me/resume",
"/api/candidates/me/profile-pic",
"/api/hrs/file/**"
);
    }

    @Override
    public void addCorsMappings(CorsRegistry registry) {
        registry.addMapping("/**")
                .allowedOriginPatterns(allowedOriginPatterns)
                .allowedMethods("*")
                .allowedHeaders("*")
                .allowCredentials(true);
    }

    @Bean
    public CorsConfigurationSource corsConfigurationSource() {
        CorsConfiguration configuration = new CorsConfiguration();
        configuration.setAllowedOriginPatterns(Arrays.asList(allowedOriginPatterns));
        configuration.setAllowedMethods(Arrays.asList("GET", "POST", "PUT", "PATCH", "DELETE", "OPTIONS"));
        configuration.setAllowedHeaders(Arrays.asList("*"));
        configuration.setExposedHeaders(Arrays.asList("Authorization", "Content-Disposition"));
        configuration.setAllowCredentials(true);

        UrlBasedCorsConfigurationSource source = new UrlBasedCorsConfigurationSource();
        source.registerCorsConfiguration("/**", configuration);
        return source;
    }
}
