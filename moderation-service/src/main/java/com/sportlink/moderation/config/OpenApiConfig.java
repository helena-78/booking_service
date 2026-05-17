package com.sportlink.moderation.config;

import io.swagger.v3.oas.models.OpenAPI;
import io.swagger.v3.oas.models.info.Contact;
import io.swagger.v3.oas.models.info.Info;
import org.springframework.context.annotation.Bean;
import org.springframework.context.annotation.Configuration;

@Configuration
public class OpenApiConfig {

    @Bean
    public OpenAPI moderationOpenAPI() {
        return new OpenAPI()
                .info(new Info()
                        .title("SportLink Moderation Service")
                        .description("Content moderation cases, verdicts, and account sanctions. "
                                + "Synchronously updates Account Management Service through a Resilience4j-protected client.")
                        .version("0.0.1")
                        .contact(new Contact().name("SportLink Team").email("group-11-esi@ut.ee")));
    }
}
