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
                        .allowedHeaders("*")
                        .exposedHeaders("Content-Type", "Authorization");

                if ("*".equals(corsOrigin)) {
                    // Permite cualquier origen (solo para desarrollo)
                    config.allowedOriginPatterns("*")
                          .allowCredentials(false);
                } else {
                    // Permite orígenes específicos (producción)
                    // Soporta múltiples orígenes separados por coma
                    String[] origins = corsOrigin.split(",");
                    for (String origin : origins) {
                        config.allowedOrigins(origin.trim());
                    }
                    config.allowCredentials(true);
                }

                config.maxAge(3600); // Cache de preflight por 1 hora
            }
        };
    }
}
