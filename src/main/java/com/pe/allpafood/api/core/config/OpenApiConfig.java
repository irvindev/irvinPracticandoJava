package com.pe.allpafood.api.core.config;

import io.swagger.v3.oas.models.Components;
import io.swagger.v3.oas.models.OpenAPI;
import io.swagger.v3.oas.models.security.SecurityRequirement;
import io.swagger.v3.oas.models.security.SecurityScheme;
import org.springdoc.core.models.GroupedOpenApi;
import org.springframework.beans.factory.annotation.Value;
import org.springframework.context.annotation.Bean;
import org.springframework.context.annotation.Configuration;
import io.swagger.v3.oas.models.servers.Server;
import java.util.List;

@Configuration
public class OpenApiConfig {

    @Value("${server.servlet.context-path}")
    private String basePath;


    @Bean
    public OpenAPI customOpenAPI() {
        return new OpenAPI()
                .components(new Components()
                        .addSecuritySchemes("bearerAuth", new SecurityScheme()
                                .type(SecurityScheme.Type.HTTP)
                                .scheme("bearer")
                                .bearerFormat("JWT")
                        )
                )
                .addSecurityItem(new SecurityRequirement().addList("bearerAuth"));
    }

    @Bean
    public GroupedOpenApi profileApi() {
        return GroupedOpenApi.builder()
                .group("Perfil de Usuario") // Nombre del grupo que se verá en Swagger UI
                .packagesToScan("com.pe.allpafood.api.gateway.profile_overview") // El paquete donde está ProfileController
                .pathsToMatch("/profile/data/**") // Solo incluirá estos paths
                .build();
    }

    @Bean
    public GroupedOpenApi dashboardApi() {
        return GroupedOpenApi.builder()
                .group("Dashboard")
                .packagesToScan("com.pe.allpafood.api.gateway.dashboard")
                .pathsToMatch("/dashboard/**")
                .build();
    }

    @Bean
    public GroupedOpenApi userPlanApi() {
        return GroupedOpenApi.builder()
                .group("User Plan")
                .packagesToScan("com.pe.allpafood.api.gateway.user_plan")
                .pathsToMatch("/plan/user/**")
                .build();
    }
}
