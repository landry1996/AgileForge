package com.agileforge.infrastructure.config;

import io.swagger.v3.oas.models.Components;
import io.swagger.v3.oas.models.OpenAPI;
import io.swagger.v3.oas.models.info.Contact;
import io.swagger.v3.oas.models.info.Info;
import io.swagger.v3.oas.models.info.License;
import io.swagger.v3.oas.models.security.SecurityRequirement;
import io.swagger.v3.oas.models.security.SecurityScheme;
import org.springframework.context.annotation.Bean;
import org.springframework.context.annotation.Configuration;

@Configuration
public class OpenApiConfig {

    @Bean
    public OpenAPI agileForgeOpenAPI() {
        return new OpenAPI()
                .info(new Info()
                        .title("AgileForge API")
                        .description("API de la plateforme AgileForge - Pilotage Projet & Delivery Intelligent")
                        .version("0.1.0")
                        .contact(new Contact()
                                .name("AgileForge Team")
                                .email("team@agileforge.io"))
                        .license(new License()
                                .name("Proprietary")))
                .addSecurityItem(new SecurityRequirement().addList("Bearer Authentication"))
                .components(new Components()
                        .addSecuritySchemes("Bearer Authentication",
                                new SecurityScheme()
                                        .type(SecurityScheme.Type.HTTP)
                                        .bearerFormat("JWT")
                                        .scheme("bearer")
                                        .description("Enter JWT token")));
    }
}
