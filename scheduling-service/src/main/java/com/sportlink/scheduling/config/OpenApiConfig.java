package com.sportlink.scheduling.config;

import io.swagger.v3.oas.models.OpenAPI;
import io.swagger.v3.oas.models.info.Contact;
import io.swagger.v3.oas.models.info.Info;
import org.springframework.context.annotation.Bean;
import org.springframework.context.annotation.Configuration;

/**
 * Customises the metadata shown at /swagger-ui.html.
 * Endpoints themselves are auto-discovered by springdoc.
 */
@Configuration
public class OpenApiConfig {

    @Bean
    public OpenAPI schedulingOpenAPI() {
        return new OpenAPI()
                .info(new Info()
                        .title("SportLink — Scheduling Service")
                        .description("Resolves user and facility availability into "
                                + "concrete bookable time slots. Used by Activity "
                                + "Management, Booking, and Matching services.")
                        .version("v1"));
    }
}