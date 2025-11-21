package com.meli.meli_ecommerce_orders_api.config;

import org.springframework.beans.factory.annotation.Value;
import org.springframework.context.annotation.Bean;
import org.springframework.context.annotation.Configuration;
import org.springframework.lang.NonNull;
import org.springframework.web.servlet.config.annotation.CorsRegistry;
import org.springframework.web.servlet.config.annotation.WebMvcConfigurer;

@Configuration
public class WebConfig {
    
    @Value("${CORS_ORIGIN:http://localhost:5173}")
    private String corsOrigin;
    
    @Bean
    public WebMvcConfigurer corsConfigurer() {
        return new WebMvcConfigurer() {
            @Override
            public void addCorsMappings(@NonNull CorsRegistry registry) {
                var config = registry.addMapping("/**")
                        .allowedMethods("GET", "POST", "PUT", "DELETE", "OPTIONS")
                        .allowedHeaders("*");
                if ("*".equals(corsOrigin)) {
                    config.allowedOriginPatterns("*")
                          .allowCredentials(false);
                } else {
                    config.allowedOrigins(corsOrigin)
                          .allowCredentials(true);
                }
            }
        };
    }
}
