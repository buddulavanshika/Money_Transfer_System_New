package com.mts.application.config;

import io.swagger.v3.oas.models.OpenAPI;
import io.swagger.v3.oas.models.Components;
import io.swagger.v3.oas.models.info.Info;
import io.swagger.v3.oas.models.security.*;
import org.springframework.context.annotation.Bean;
import org.springframework.context.annotation.Configuration;

@Configuration
public class OpenApiConfig {

        @Bean
        public OpenAPI api() {
                final String securitySchemeName = "BearerAuthentication";
                return new OpenAPI()
                                .info(new Info()
                                                .title("Money Transfer API")
                                                .description("Secure REST API for managing accounts and money transfers")
                                                .version("v1.0.0")
                                                .license(new io.swagger.v3.oas.models.info.License().name("Apache 2.0")
                                                                .url("http://springdoc.org")))
                                .externalDocs(new io.swagger.v3.oas.models.ExternalDocumentation()
                                                .description("Money Transfer API Documentation")
                                                .url("https://example.com/docs/money-transfer"))
                                .components(new Components()
                                                .addSecuritySchemes(securitySchemeName,
                                                                new SecurityScheme()
                                                                                .name(securitySchemeName)
                                                                                .type(SecurityScheme.Type.HTTP)
                                                                                .scheme("bearer")
                                                                                .bearerFormat("JWT")))
                                .addSecurityItem(new SecurityRequirement().addList(securitySchemeName));
        }
}
