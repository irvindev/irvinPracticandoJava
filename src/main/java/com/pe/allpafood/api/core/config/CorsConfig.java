package com.pe.allpafood.api.core.config;


import org.springframework.beans.factory.annotation.Value;
import org.springframework.context.annotation.Bean;
import org.springframework.context.annotation.Configuration;
import org.springframework.context.annotation.Profile;
import org.springframework.web.servlet.config.annotation.CorsRegistry;
import org.springframework.web.servlet.config.annotation.WebMvcConfigurer;

import java.util.List;

@Configuration
@Profile({"dev"})
public class CorsConfig {

    @Value("${security.cors.front.url}")
    private List<String> frontDomains;

    @Value("${security.cors.allow}")
    private boolean allowCredentials;

    @Bean
    public WebMvcConfigurer corsConfigurer() {
        return new WebMvcConfigurer() {
            @Override
            public void addCorsMappings(CorsRegistry registry) {
                registry.addMapping("/**")
                        .allowedOriginPatterns(frontDomains.toArray(new String[0]))
                        .allowedMethods("*")
                        .allowCredentials(allowCredentials);
            }
        };
    }
}