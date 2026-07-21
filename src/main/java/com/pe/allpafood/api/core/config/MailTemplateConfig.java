package com.pe.allpafood.api.core.config;

import org.springframework.context.annotation.Bean;
import org.springframework.context.annotation.Configuration;
import org.thymeleaf.spring6.SpringTemplateEngine;
import org.thymeleaf.templateresolver.ClassLoaderTemplateResolver;

@Configuration
public class MailTemplateConfig {

    @Bean
    public SpringTemplateEngine mailTemplateEngine() {
        SpringTemplateEngine engine = new SpringTemplateEngine();

        ClassLoaderTemplateResolver resolver = new ClassLoaderTemplateResolver();
        resolver.setPrefix("templates/mail/");   // src/main/resources/templates/mail
        resolver.setSuffix(".html");
        resolver.setTemplateMode("HTML");
        resolver.setCharacterEncoding("UTF-8");

        engine.setTemplateResolver(resolver);
        return engine;
    }
}