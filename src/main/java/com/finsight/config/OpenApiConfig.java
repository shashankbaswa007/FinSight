package com.finsight.config;

import io.swagger.v3.oas.annotations.enums.SecuritySchemeType;
import io.swagger.v3.oas.annotations.security.SecurityScheme;
import io.swagger.v3.oas.models.OpenAPI;
import io.swagger.v3.oas.models.info.Contact;
import io.swagger.v3.oas.models.info.Info;
import io.swagger.v3.oas.models.info.License;
import io.swagger.v3.oas.models.security.SecurityRequirement;
import org.springframework.context.annotation.Bean;
import org.springframework.context.annotation.Configuration;

/**
 * OpenAPI / Swagger configuration.
 * Adds JWT Bearer authentication support to the Swagger UI.
 */
@Configuration
@SecurityScheme(
        name = "bearerAuth",
        type = SecuritySchemeType.HTTP,
        scheme = "bearer",
        bearerFormat = "JWT",
        description = "Enter your JWT token (without the 'Bearer ' prefix)"
)
public class OpenApiConfig {

    @Bean
    public OpenAPI finSightOpenAPI() {
        return new OpenAPI()
                .info(new Info()
                        .title("FinSight API")
                        .description("Personal Finance Analytics Platform – REST API Documentation")
                        .version("1.0.0")
                        .contact(new Contact()
                                .name("FinSight Team")
                                .email("support@finsight.dev"))
                        .license(new License()
                                .name("MIT License")
                                .url("https://opensource.org/licenses/MIT")))
                .addSecurityItem(new SecurityRequirement().addList("bearerAuth"));
    }
}
