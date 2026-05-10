package com.sportlink.activitymanagement.config;

import io.swagger.v3.oas.models.OpenAPI;
import io.swagger.v3.oas.models.info.Contact;
import io.swagger.v3.oas.models.info.Info;
import org.springframework.context.annotation.Bean;
import org.springframework.context.annotation.Configuration;

@Configuration
public class OpenApiConfig {

    @Bean
    public OpenAPI activityManagementOpenAPI() {
        return new OpenAPI()
                .info(new Info()
                        .title("SportLink - Activity Management Service")
                        .description("Manages the lifecycle of sports activities: creation, "
                                + "updates, cancellation, join/leave, and participant management.")
                        .version("v1"));
    }
}