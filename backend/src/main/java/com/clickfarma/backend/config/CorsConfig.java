package com.clickfarma.backend.config;

import org.springframework.beans.factory.annotation.Value;
import org.springframework.context.annotation.Configuration;
import org.springframework.web.servlet.config.annotation.CorsRegistry;
import org.springframework.web.servlet.config.annotation.WebMvcConfigurer;

@Configuration
public class CorsConfig implements WebMvcConfigurer {

    @Value("${app.frontend.url:*}")
    private String frontendUrl;

    @Override
    public void addCorsMappings(CorsRegistry registry) {
        String cleanFrontendUrl = frontendUrl != null ? frontendUrl.replaceAll("/+$", "") : "";
        registry.addMapping("/**")
                .allowedOriginPatterns("http://localhost", "http://127.0.0.1", "http://localhost:8081", "https://*.onrender.com", "http://*.onrender.com", cleanFrontendUrl)
                .allowedMethods("GET", "POST", "PUT", "DELETE", "PATCH", "OPTIONS")
                .allowedHeaders("*")
                .allowCredentials(true)
                .maxAge(3600);
    }
}