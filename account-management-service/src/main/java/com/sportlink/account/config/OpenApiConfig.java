package com.sportlink.account.config;

import io.swagger.v3.oas.models.OpenAPI;
import io.swagger.v3.oas.models.info.Contact;
import io.swagger.v3.oas.models.info.Info;
import org.springframework.context.annotation.Bean;
import org.springframework.context.annotation.Configuration;

@Configuration
public class OpenApiConfig {

    @Bean
    public OpenAPI accountManagementOpenAPI() {
        return new OpenAPI()
                .info(new Info()
                        .title("SportLink Account Management Service")
                        .description("Registration, authentication, and profile management for users and sport centers.")
                        .version("0.0.1")
                        .contact(new Contact().name("SportLink Team").email("group-11-esi@ut.ee")));
    }
}
