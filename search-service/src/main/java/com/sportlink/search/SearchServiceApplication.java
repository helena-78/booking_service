package com.sportlink.search;

import org.springframework.boot.SpringApplication;
import org.springframework.boot.autoconfigure.SpringBootApplication;
import org.springframework.context.annotation.Bean;
import org.springframework.web.reactive.function.client.WebClient;

@SpringBootApplication
public class SearchServiceApplication {

    /*
     * WebClient bean used by the Search Service to make synchronous calls to
     * the Booking Service (facility data lookup).
     */
    @Bean
    public WebClient.Builder getWebClientBuilder() {
        return WebClient.builder();
    }

    public static void main(String[] args) {
        DatabaseInitializer.initialize("searchservice_db");
        SpringApplication.run(SearchServiceApplication.class, args);
    }

}
